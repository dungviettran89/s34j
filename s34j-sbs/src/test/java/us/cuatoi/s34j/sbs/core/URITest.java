package us.cuatoi.s34j.sbs.core;

import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URITest {
    @Test
    public void testURIParts() throws MalformedURLException, URISyntaxException {
        URI uri = new File("test.txt").toURI();
        printOutInfo(uri);

        uri = new URL("https://www.google.com").toURI();
        printOutInfo(uri);
    }

    private void printOutInfo(URI uri) {
        System.out.println("uri=" + uri.toString());
        System.out.println("uri.scheme=" + uri.getScheme());
        System.out.println("uri.userInfo=" + uri.getUserInfo());
        System.out.println("uri.host=" + uri.getHost());
        System.out.println("uri.port=" + uri.getPort());
        System.out.println("uri.path=" + uri.getPath());
        System.out.println("uri.query=" + uri.getQuery());
        System.out.println("uri.fragment=" + uri.getFragment());
    }
}
