package edu.kit.aifb.summa.servlet;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionMapperImpl implements ExceptionMapper<Exception> {
  public Response toResponse(Exception e) {
	e.printStackTrace();
    return Response.status(500).
      type("text/plain").
      entity("\nError! Please check if the mandatory query parameters \"entity\" or \"topK\" "
      		+ "are present.").
      build();
  }
}
