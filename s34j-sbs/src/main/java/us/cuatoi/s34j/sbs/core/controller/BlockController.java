package us.cuatoi.s34j.sbs.core.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("${s34j.sbs.url:blocks}")
public class BlockController {

    private static final Logger logger = LoggerFactory.getLogger(BlockController.class);
    @Value("${s34j.sbs.url:blocks}")
    private String basePath;

    /**
     * Perform get operation on a block. Return the whole block to the client.
     * Seeking is not supported.
     *
     * @param request  raw http request
     * @param response raw http response to return to client
     * @throws IOException if something bad happens
     */
    @RequestMapping(value = "/**", method = RequestMethod.GET, params = {"!list", "!attribute"})
    public void getBlock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String key = getKey(request);
        logger.info(".getBlock(): key=" + key);
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        response.getWriter().write(key);
    }

    /**
     * Perform a create / update operation on a block.
     *
     * @param request  request raw http request
     * @param response raw http response to return to client
     * @throws IOException if something bad happens
     */
    @RequestMapping(value = "/**", method = RequestMethod.POST, params = "!attribute")
    public void postBlock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String key = getKey(request);
        logger.info(".postBlock(): key=" + key);
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        response.getWriter().write(key);
    }

    /**
     * Delete a block
     *
     * @param request  raw http request
     * @param response raw http response
     * @throws IOException if something bad happens
     */
    @RequestMapping(value = "/**", method = RequestMethod.DELETE, params = "!attribute")
    public void deleteBlock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String key = getKey(request);
        logger.info(".deleteBlock(): key=" + key);
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        response.getWriter().write(key);
    }

    /**
     * List all the block which has the same prefix
     *
     * @param request  raw request to parse prefix from
     * @param pageSize limit the response size
     * @param token    the next token used for paging
     */
    @RequestMapping(value = "/**", method = RequestMethod.GET, params = {"list", "!attribute"}, produces = "application/json; charset=utf-8")
    @ResponseBody
    public String listBlock(HttpServletRequest request,
                            @RequestParam(value = "limit", defaultValue = "30", required = false) int pageSize,
                            @RequestParam(value = "token", required = false) String token) {
        String prefix = getKey(request);
        logger.info(".listBlock(): prefix=" + prefix);
        logger.info(".listBlock(): pageSize=" + pageSize);
        logger.info(".listBlock(): token=" + token);
        return "Not implemented";
    }

    private String getKey(HttpServletRequest request) {
        String key = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        key = StringUtils.replace(key, "/" + basePath, "");
        key = StringUtils.trimToEmpty(key);
        key = key.isEmpty() ? "/" : key;
        return key;
    }
}
