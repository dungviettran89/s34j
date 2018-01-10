package us.cuatoi.s34jserver.core.operation;

import us.cuatoi.s34jserver.core.S3Context;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.dto.BucketDTO;
import us.cuatoi.s34jserver.core.dto.BucketsDTO;
import us.cuatoi.s34jserver.core.dto.ListAllMyBucketsResultDTO;
import us.cuatoi.s34jserver.core.dto.OwnerDTO;
import us.cuatoi.s34jserver.core.model.GetBucketsS3Request;
import us.cuatoi.s34jserver.core.model.GetBucketsS3Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import static us.cuatoi.s34jserver.core.S3Constants.EXPIRATION_DATE_FORMAT;

public class GetBucketsS3RequestHandler extends S3RequestHandler<GetBucketsS3Request, GetBucketsS3Response> {
    public GetBucketsS3RequestHandler(S3Context context, GetBucketsS3Request s3Request) {
        super(context, s3Request);
    }

    @Override
    public GetBucketsS3Response handle() throws IOException {
        BucketsDTO b = new BucketsDTO();
        Files.list(baseDir).forEach((p) -> {
            try {
                Verifier.verifyBucketName(p.getFileName().toString());
                BasicFileAttributes attribute = Files.readAttributes(p, BasicFileAttributes.class);
                BucketDTO br = new BucketDTO();
                br.setName(p.getFileName().toString());
                br.setCreationDate(EXPIRATION_DATE_FORMAT.print(attribute.creationTime().toMillis()));
                b.getBucketList().add(br);
            } catch (S3Exception ex) {
                logger.debug("Ignored:" + p.getFileName().toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        OwnerDTO or = new OwnerDTO();
        or.setId(context.getServerId());
        or.setDisplayName(context.getServerId());

        ListAllMyBucketsResultDTO content = new ListAllMyBucketsResultDTO();
        content.setOwner(or);
        content.setBuckets(b);
        return (GetBucketsS3Response) new GetBucketsS3Response(s3Request).setContent(content);
    }
}
