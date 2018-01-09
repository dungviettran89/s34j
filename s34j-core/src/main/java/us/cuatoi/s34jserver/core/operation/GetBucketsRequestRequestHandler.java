package us.cuatoi.s34jserver.core.operation;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.dto.BucketResponse;
import us.cuatoi.s34jserver.core.dto.BucketsResponse;
import us.cuatoi.s34jserver.core.dto.ListAllMyBucketsResult;
import us.cuatoi.s34jserver.core.dto.OwnerResponse;
import us.cuatoi.s34jserver.core.model.GetBucketsRequest;
import us.cuatoi.s34jserver.core.model.GetBucketsResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import static us.cuatoi.s34jserver.core.S3Constants.EXPIRATION_DATE_FORMAT;

public class GetBucketsRequestRequestHandler extends S3RequestHandler<GetBucketsRequest, GetBucketsResponse> {
    public GetBucketsRequestRequestHandler(S3Context context, GetBucketsRequest s3Request) {
        super(context, s3Request);
    }

    @Override
    public GetBucketsResponse handle() throws IOException {
        BucketsResponse b = new BucketsResponse();
        Files.list(baseDir).forEach((p) -> {
            try {
                Verifier.verifyBucketName(p.getFileName().toString());
                BasicFileAttributes attribute = Files.readAttributes(p, BasicFileAttributes.class);
                BucketResponse br = new BucketResponse();
                br.setName(p.getFileName().toString());
                br.setCreationDate(EXPIRATION_DATE_FORMAT.print(attribute.creationTime().toMillis()));
                b.getBucketList().add(br);
            } catch (S3Exception ex) {
                logger.debug("Ignored:" + p.getFileName().toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        OwnerResponse or = new OwnerResponse();
        or.setId(context.getServerId());
        or.setDisplayName(context.getServerId());

        ListAllMyBucketsResult content = new ListAllMyBucketsResult();
        content.setOwner(or);
        content.setBuckets(b);
        return (GetBucketsResponse) new GetBucketsResponse(s3Request).setContent(content);
    }
}
