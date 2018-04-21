package us.cuatoi.s34j.spring.operation.bucket;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.dto.BucketXml;
import us.cuatoi.s34j.spring.dto.BucketsXml;
import us.cuatoi.s34j.spring.dto.ListAllMyBucketsResultXml;
import us.cuatoi.s34j.spring.dto.OwnerXml;
import us.cuatoi.s34j.spring.helper.DateHelper;
import us.cuatoi.s34j.spring.model.BucketRepository;
import us.cuatoi.s34j.spring.operation.ExecutionRule;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static us.cuatoi.s34j.spring.SpringStorageConstants.CONTENT_TYPE;
import static us.cuatoi.s34j.spring.SpringStorageConstants.EXPIRATION_DATE_FORMAT;

@Service
@Rule(name = "ListAllBuckets")
public class ListAllBuckets implements ExecutionRule {
    @Autowired
    private BucketRepository bucketRepository;

    @Condition
    public boolean shouldRun(@Fact("GET") boolean isGet, @Fact("path") String path) {
        return StringUtils.equalsIgnoreCase(path, "/");
    }

    @Action
    public void returnBucketList(Facts facts, @Fact("awsAccessKey") String awsAccessKey) {
        List<BucketXml> bucketList = Lists.newArrayList(bucketRepository.findAll()).stream()
                .map((b) -> {
                    Date createdDate = new Date(b.getCreatedDate());
                    BucketXml xml = new BucketXml();
                    xml.setName(b.getName());
                    xml.setCreationDate(DateHelper.format(EXPIRATION_DATE_FORMAT, createdDate));
                    return xml;
                })
                .collect(Collectors.toList());
        BucketsXml buckets = new BucketsXml();
        buckets.setBucketList(bucketList);
        OwnerXml ownerXml = new OwnerXml();
        ownerXml.setId(awsAccessKey);
        ownerXml.setDisplayName(awsAccessKey);
        ListAllMyBucketsResultXml response = new ListAllMyBucketsResultXml();
        response.setOwner(ownerXml);
        response.setBuckets(buckets);
        facts.put("statusCode", 200);
        facts.put("contentType", CONTENT_TYPE);
        facts.put("response", response);
    }
}
