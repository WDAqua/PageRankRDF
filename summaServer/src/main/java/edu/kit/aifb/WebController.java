package edu.kit.aifb;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.kit.aifb.model.TripleMeta;
import edu.kit.aifb.model.URI;
import edu.kit.aifb.summarizer.Summarizer;
import edu.kit.aifb.summarizer.implemented.DBLP;
import edu.kit.aifb.summarizer.implemented.MusicBrainz;

@Controller
public class WebController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    //Set this to allow browser requests from other websites
    @ModelAttribute
    public void setVaryResponseHeader(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    // path of this service
    @org.springframework.beans.factory.annotation.Value("${server.host}")
    private String PATH;

    // SUMMA vocabulary
    private static final String SUMMARY = "http://purl.org/voc/summa/Summary";
    private static final String ENTITY = "http://purl.org/voc/summa/entity";
    private static final String TOP_K = "http://purl.org/voc/summa/topK";
    private static final String MAX_HOPS = "http://purl.org/voc/summa/maxHops";
    private static final String LANGUAGE = "http://purl.org/voc/summa/language";
    private static final String KB = "http://purl.org/voc/summa/kb";
    private static final String STATEMENT = "http://purl.org/voc/summa/statement";
    private static final String FIXED_PROPERTY = "http://purl.org/voc/summa/fixedProperty";

    // vrank vocabulary
    private static final String HAS_RANK = "http://purl.org/voc/vrank#hasRank";
    private static final String RANK_VALUE = "http://purl.org/voc/vrank#rankValue";
    private List<Summarizer> summerizer;


    @Autowired
    public WebController(List<Summarizer> summerizer) {
        this.summerizer = summerizer;
    }

    @RequestMapping(method = RequestMethod.POST, value="/sum")
    public ResponseEntity<?> summaPost(
            @RequestHeader("Content-Type") String inputMime,
            @RequestHeader("Accept") String outputMime,
            @RequestBody String message){
        // get formats for MIME types
        RDFFormat inputFormat = Rio.getParserFormatForMIMEType(inputMime);
        RDFFormat outputFormat = Rio.getParserFormatForMIMEType(outputMime.split(",")[0]);
        if (inputFormat == null) {
            // TODO
            // method which detects the format
            // if Format could not be detected leave method and return error
        }

        if (outputFormat == null) {
            outputFormat = RDFFormat.TURTLE;
        }

        try {
            Model model = Rio.parse(new StringReader(message), "", inputFormat);

            ValueFactory f = ValueFactoryImpl.getInstance();
            String entity = model.filter(null, f.createURI(ENTITY), null).objectURI().stringValue();
            Integer topK = Integer.parseInt(model.filter(null, f.createURI(TOP_K), null).objectValue().stringValue());

            Model maxHopsMod = model.filter(null, f.createURI(MAX_HOPS), null);
            Integer maxHops = null;
            if (!maxHopsMod.isEmpty()) {
                maxHops = Integer.parseInt(maxHopsMod.objectValue().stringValue());
            }

            String language = null;
            Model languageMod = model.filter(null, f.createURI(LANGUAGE), null);
            if (!languageMod.isEmpty()) {
                language = languageMod.objectValue().stringValue();
            }
            String kb = null;
            Model kbMod = model.filter(null, f.createURI(KB), null);
            if (!kbMod.isEmpty()) {
                kb = kbMod.objectValue().stringValue();
            }
            Model m = model.filter(null, f.createURI(FIXED_PROPERTY), null);
            Set<Value> objects = m.objects();
            Iterator<Value> val = objects.iterator();
            String [] fixedProperties = new String [objects.size()];
            for (int i = 0; i < fixedProperties.length; i++) {
                fixedProperties[i] = val.next().stringValue();
            }

            return executeQuery(entity, topK, maxHops, fixedProperties, language, kb, outputFormat);

        } catch (RDFParseException e) {
            e.printStackTrace();
        } catch (UnsupportedRDFormatException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @RequestMapping(method = RequestMethod.OPTIONS, value="/sum")
    public ResponseEntity<?> getOptions() {
        return ResponseEntity.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, OPTIONS").build();
    }


    @RequestMapping(method = RequestMethod.GET, value="/sum")
    public ResponseEntity<?> summa(@RequestParam(value="entity") String entity,
                        @RequestParam(value="topK", defaultValue = "5") Integer topK,
                        @RequestParam(value="maxHops", defaultValue = "1") Integer maxHops,
                        @RequestParam(value="fixedProperty", defaultValue ="") String fixedProperty,
                        @RequestParam(value="language", defaultValue = "en") String language,
                        @RequestHeader(value="Accept") String header,
                        @RequestParam(value="kb", defaultValue = "dblp") String kb,
                        @RequestHeader("Accept") String outputMime) {
        RDFFormat outputFormat = Rio.getParserFormatForMIMEType(outputMime.split(",")[0]);
        if (outputFormat == null) {
            outputFormat = RDFFormat.TURTLE;
        }
        String [] fixedProperties = new String[0];
        if (!fixedProperty.equals("")) {
            fixedProperties = fixedProperty.split(",");
        }
        return executeQuery(entity, topK, maxHops, fixedProperties, language, kb, outputFormat);
        //old Response.fromResponse(r).status(200).header("Location", null).build();
    }

    private ResponseEntity<?> executeQuery(String entity, Integer topK, Integer maxHops,
                                  String [] fixedProperties, String language, String kb, RDFFormat outputFormat) {
        logger.info("Request kb: {}, entity: {}, topk: {}",kb,entity,topK);
        ValueFactory f = ValueFactoryImpl.getInstance();
        String mime = outputFormat.getMIMETypes().get(0);

        entity = filter(entity);
        language = filter(language);
        for (String string : fixedProperties) {
            string = filter(string);
        }

        Summarizer summarizer = null;
        boolean found = false;
        for (Summarizer s : summerizer){

            if (s.getName().equals(kb)){
                summarizer = s;
                found = true;
            }

        }
        if (!found){
            logger.info("No summerizer found for endpoint {}",kb);
        } else {

            List<TripleMeta> res = null;
            java.net.URI uri = null;
            try {
                uri = new java.net.URI(entity);

                res = summarizer.summarize(uri, fixedProperties, topK, maxHops, language);

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            Model result = createModel(entity, topK, maxHops, fixedProperties, language, res);
            String r = result.filter(null, RDF.TYPE, f.createURI(SUMMARY)).subjects().iterator().next().stringValue();
            StringWriter writer = new StringWriter();
            try {
                Rio.write(result, writer, outputFormat);
                String s = writer.toString();
                return ResponseEntity.created(new java.net.URI(r)).header("Content-Type", mime).
                        header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                        .body(s);
            } catch (RDFHandlerException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Model createModel(String entity, Integer topK, Integer maxHops,
                              String [] fixedProperty, String language, List<TripleMeta> meta) {
        Model model = new LinkedHashModel();
        ValueFactory f = ValueFactoryImpl.getInstance();
        String uri = PATH + "?entity=" + entity + "&topK=" + topK;
        if (maxHops != null) {
            uri += "&maxHops=" + maxHops;
        }
        if (language != null) {
            uri += "&language=" + language;
        }
        if (fixedProperty.length > 0) {
            uri += "&fixedProperty=";
        }
        for (String property : fixedProperty) {
            uri += property + ",";
        }
        if (uri.endsWith(",")) {
            uri = uri.substring(0, uri.length() - 1);
        }

        Resource r = null;
        r = f.createURI(uri);
        model.add(f.createStatement(r, RDF.TYPE, f.createURI(SUMMARY)));
        model.add(f.createStatement(r, f.createURI(ENTITY), f.createURI(entity)));
        model.add(f.createStatement(r, f.createURI(TOP_K), f.createLiteral(topK)));
        if (maxHops != null) {
            model.add(f.createStatement(r, f.createURI(MAX_HOPS), f.createLiteral(maxHops)));
        }
        if (language != null) {
            model.add(f.createStatement(r, f.createURI(LANGUAGE), f.createLiteral(language)));
        }
        for (String property : fixedProperty) {
            model.add(f.createStatement(r, f.createURI(FIXED_PROPERTY), f.createURI(property)));
        }
        for (TripleMeta triple : meta) {
            Resource stmt = f.createBNode();
            model.add(f.createStatement(stmt, RDF.TYPE, RDF.STATEMENT));
            model.add(f.createStatement(stmt, RDF.SUBJECT, f.createURI(triple.getSubject().getURI().toString())));
            model.add(f.createStatement(stmt, RDF.PREDICATE, f.createURI(triple.getPredicate().getURI().toString())));
            model.add(f.createStatement(stmt, RDF.OBJECT, f.createURI(((URI) triple.getObject()).getURI().toString())));

            // add labels
            String subjectLabel = triple.getSubject().getLabel();
            String subjectLabelLang = triple.getSubject().getLang();
            String predicateLabel = triple.getPredicate().getLabel();
            String predicateLabelLang = triple.getSubject().getLang();
            String objectLabel = ((URI) triple.getObject()).getLabel();
            String objectLabelLang = triple.getSubject().getLang();
            if (subjectLabel != null) {
                model.add(f.createStatement(f.createURI(triple.getSubject().getURI().toString()), RDFS.LABEL, f.createLiteral(subjectLabel, subjectLabelLang)));
            }
            if (predicateLabel != null) {
                model.add(f.createStatement(f.createURI(triple.getPredicate().getURI().toString()), RDFS.LABEL, f.createLiteral(predicateLabel, predicateLabelLang)));
            }
            if (objectLabel != null) {
                model.add(f.createStatement(f.createURI(((URI) triple.getObject()).getURI().toString()), RDFS.LABEL, f.createLiteral(objectLabel, objectLabelLang)));
            }

            model.add(f.createStatement(r, f.createURI(STATEMENT), stmt));
            Resource rank = f.createBNode();
            model.add(f.createStatement(stmt, f.createURI(HAS_RANK), rank));
            model.add(f.createStatement(rank, f.createURI(RANK_VALUE), f.createLiteral(triple.getRank())));
            model.add(f.createStatement(f.createURI(uri+"#id"), OWL.SAMEAS, f.createURI(entity)));
        }

        return model;

    }

    private String filter(String string) {
        if (string == null) {
            return string;
        }
        if (string.startsWith("\"") && string.endsWith("\"")) {
            string = string.substring(1, string.length() - 1);
        }
        if (string.startsWith("\'") && string.endsWith("\'")) {
            string = string.substring(1, string.length() - 1);
        }
        if (string.startsWith("<") && string.endsWith(">")) {
            string = string.substring(1, string.length() - 1);
        }
        return string;
    }
}