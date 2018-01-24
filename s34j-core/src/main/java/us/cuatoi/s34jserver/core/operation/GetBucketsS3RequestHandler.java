package us.cuatoi.s34jserver.core.operation;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.dto.BucketXml;
import us.cuatoi.s34jserver.core.dto.BucketsXml;
import us.cuatoi.s34jserver.core.dto.ListAllMyBucketsResultXml;
import us.cuatoi.s34jserver.core.dto.OwnerXml;
import us.cuatoi.s34jserver.core.model.GetBucketsS3Request;
import us.cuatoi.s34jserver.core.model.GetBucketsS3Response;

import java.io.IOException;
import java.nio.file.Files;

import static us.cuatoi.s34jserver.core.helper.PathHelper.getCreationTimeString;

public class GetBucketsS3RequestHandler extends S3RequestHandler<GetBucketsS3Request, GetBucketsS3Response> {
    public GetBucketsS3RequestHandler(S3Context context, GetBucketsS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    public GetBucketsS3Response handle() throws IOException {
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
        return (GetBucketsS3Response) new GetBucketsS3Response(s3Request).setContent(content);
    }
}
