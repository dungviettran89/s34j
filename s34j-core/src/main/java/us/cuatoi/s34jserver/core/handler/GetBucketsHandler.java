package us.cuatoi.s34jserver.core.handler;

import us.cuatoi.s34jserver.core.Request;
import us.cuatoi.s34jserver.core.Response;
import us.cuatoi.s34jserver.core.S3Constants;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.dto.BucketXml;
import us.cuatoi.s34jserver.core.dto.BucketsXml;
import us.cuatoi.s34jserver.core.dto.ListAllMyBucketsResultXml;
import us.cuatoi.s34jserver.core.dto.OwnerXml;
import us.cuatoi.s34jserver.core.operation.Verifier;
import us.cuatoi.s34jserver.core.servlet.SimpleStorageContext;

import java.io.IOException;
import java.nio.file.Files;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static us.cuatoi.s34jserver.core.helper.PathHelper.getCreationTimeString;

public class GetBucketsHandler extends BaseHandler {

    protected GetBucketsHandler(SimpleStorageContext context, Request request) {
        super(context, request);
    }

    @Override
    public Response handle() throws Exception {
        BucketsXml b = new BucketsXml();
        Files.list(baseDir).forEach((p) -> {
            try {
                Verifier.verifyBucketName(p.getFileName().toString());
                BucketXml br = new BucketXml();
                br.setName(p.getFileName().toString());
                br.setCreationDate(getCreationTimeString(p));
                b.getBucketList().add(br);
            } catch (S3Exception ex) {
                logger.debug("Ignored:" + p.getFileName().toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        OwnerXml or = new OwnerXml();
        or.setId(context.getServerId());
        or.setDisplayName(context.getServerId());

        ListAllMyBucketsResultXml content = new ListAllMyBucketsResultXml();
        content.setOwner(or);
        content.setBuckets(b);
        return new Response()
                .setContentType(S3Constants.CONTENT_TYPE_XML)
                .setContent(content);
    }

    public static class Builder extends BaseHandler.Builder {

        @Override
        public boolean canHandle(Request request) {
            return equalsIgnoreCase(request.getMethod(),"get") &&
                    isBlank(request.getBucketName())
                    && isBlank(request.getObjectName());
        }

        @Override
        public BaseHandler create(SimpleStorageContext context, Request request) {
            return new GetBucketsHandler(context, request);
        }
    }
}
