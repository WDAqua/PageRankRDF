package edu.kit.aifb.summa;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import edu.kit.aifb.summa.model.TripleMeta;
import edu.kit.aifb.summa.model.URI;
import edu.kit.aifb.summa.summarizer.Summarizer;
import edu.kit.aifb.summa.summarizer.SummarizerDBLP;
import edu.kit.aifb.summa.summarizer.SummarizerMusicBrainz;

@RestController
public class WebController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    //Set this to allow browser requests from other websites

    // path of this service
    private static final String PATH = "http://km.aifb.kit.edu/summaServer/sum";

    // SUMMA vocabulary
    private static final String SUMMARY = "http://purl.org/voc/summa/Summary";
    private static final String ENTITY = "http://purl.org/voc/summa/entity";
    private static final String TOP_K = "http://purl.org/voc/summa/topK";
    private static final String MAX_HOPS = "http://purl.org/voc/summa/maxHops";
    private static final String LANGUAGE = "http://purl.org/voc/summa/language";
    private static final String STATEMENT = "http://purl.org/voc/summa/statement";
    private static final String FIXED_PROPERTY = "http://purl.org/voc/summa/fixedProperty";

    // vrank vocabulary
    private static final String HAS_RANK = "http://purl.org/voc/vrank#hasRank";
    private static final String RANK_VALUE = "http://purl.org/voc/vrank#rankValue";

    @ModelAttribute
    public void setVaryResponseHeader(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    @RequestMapping("/")
    public String summa(@RequestParam(value="entity") String entity,
                        @RequestParam(value="kb") String kb,
                        @RequestParam(value="topK", defaultValue = "5") Integer topK,
                        @RequestParam(value="fixedProperty", defaultValue ="") String[] fixedProperties,
                        @RequestParam(value="language", defaultValue = "en") String language,
                        @RequestParam(value="maxHops", defaultValue = "1") Integer maxHops,
                        @RequestHeader(value="Accept") String header) {
        RDFFormat outputFormat = Rio.getParserFormatForMIMEType(header.split(",")[0]);
        if (outputFormat == null) {
            outputFormat = RDFFormat.TURTLE;
        }
        Summarizer summarizer = null;
        if (kb.equals("dblp")){
            summarizer = new SummarizerDBLP();
        } else if (kb.equals("musicbrainz")){
            summarizer = new SummarizerMusicBrainz();
        } else {
            logger.info("The endpoint is not dblp nor musicbrainz");
        }

        //logger.info("Request sparql: {}, lang: {}, kb: {}",sparql,lang,kb,endpoint);


        entity = filter(entity);
        language = filter(language);
        for (String string : fixedProperties) {
            string = filter(string);
        }

        List<TripleMeta> res = null;
        java.net.URI uri = null;
        try {
            uri = new java.net.URI(entity);
            res = summarizer.summarize(uri, fixedProperties, topK, maxHops, language);

        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Model result = createModel(entity, topK, maxHops, fixedProperties, language, res);

        Iterator<Statement> i = result.iterator();
        while (i.hasNext()){
            Statement s = i.next();
            System.out.println(s.getSubject().toString()+" --- "+s.getPredicate().toString()+" --- "+s.getObject().toString());
        }

        ValueFactory f = ValueFactoryImpl.getInstance();

        StringWriter writer = new StringWriter();
        try {
            Rio.write(result, writer, outputFormat);
        } catch (RDFHandlerException e) {
            e.printStackTrace();
        }
        String s = writer.toString();
        return s;
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
            System.out.println(subjectLabel);
            System.out.println(predicateLabel);
            System.out.println(objectLabel);
            System.out.println(subjectLabelLang);
            System.out.println(predicateLabelLang);
            System.out.println(objectLabelLang);
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
