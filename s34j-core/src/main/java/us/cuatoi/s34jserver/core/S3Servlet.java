package us.cuatoi.s34jserver.core;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class S3Servlet extends HttpServlet {

    private final S3Context s3Context;

    public S3Servlet(S3Context s3Context) {
        this.s3Context = s3Context;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        new S3Handler(s3Context,request,response).handle();
    }
}
