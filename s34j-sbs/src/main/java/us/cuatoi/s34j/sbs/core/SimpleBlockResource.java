package us.cuatoi.s34j.sbs.core;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Controller
@ConditionalOnClass(RequestMapping.class)
@RequestMapping("${s34j.sbs.SimpleBlockResource.path:blocks}")
public class SimpleBlockResource {
    private final static Logger logger = LoggerFactory.getLogger(SimpleBlockResource.class);
    @Autowired
    SimpleBlockStorage simpleBlockStorage;

    @RequestMapping(value = "_status", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @ResponseBody
    public StoreStatus status(@RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
        return simpleBlockStorage.status(refresh);
    }


    @RequestMapping(value = "{key:[a-zA-Z0-9.\\-]+}", method = RequestMethod.GET)
    public void load(@PathVariable("key") String key, HttpServletResponse response) throws IOException {
        logger.info("load(" + key + ")");
        try {
            StoreHelper.validateKey(key);
            try (InputStream is = simpleBlockStorage.load(key)) {
                long length = ByteStreams.copy(is, response.getOutputStream());
                logger.info("load(" + key + "): length=" + length);
            }
        } catch (FileNotFoundException loadError) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, key + " not found.");
        } catch (IllegalArgumentException badRequest) {
            logger.warn("load(" + key + "): badRequest=" + badRequest, badRequest);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, badRequest.getMessage());
        }
    }

    @RequestMapping(value = "{key:[a-zA-Z0-9.\\-]+}", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<String> save(@PathVariable("key") String key,
                                       HttpServletRequest httpServletRequest) throws IOException {
        logger.info("save(" + key + ")");
        try {
            StoreHelper.validateKey(key);
            long length = simpleBlockStorage.save(key, httpServletRequest.getInputStream());
            logger.info("save(" + key + "): length=" + length);
            return ResponseEntity.ok(key + " saved.");
        } catch (IllegalArgumentException badRequest) {
            logger.warn("load(" + key + "): badRequest=" + badRequest, badRequest);
            return ResponseEntity.badRequest().body(badRequest.getMessage());
        }
    }

    @RequestMapping(value = "{key:[a-zA-Z0-9.\\-]+}", method = RequestMethod.DELETE)
    public ResponseEntity<String> delete(@PathVariable("key") String key) {
        logger.info("delete(" + key + ")");
        try {
            StoreHelper.validateKey(key);
            simpleBlockStorage.delete(key);
            return ResponseEntity.ok(key + " deleted.");
        } catch (IllegalArgumentException badRequest) {
            logger.warn("delete(" + key + "): badRequest=" + badRequest, badRequest);
            return ResponseEntity.badRequest().body(badRequest.getMessage());
        } catch (FileNotFoundException deleteError) {
            return ResponseEntity.notFound().build();
        }
    }
}
