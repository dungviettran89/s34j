package us.cuatoi.s34j.sbs.test;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.google.gson.Gson;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.xmlpull.v1.XmlPullParserException;
import us.cuatoi.s34j.sbs.core.operation.AvailabilityUpdater;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationModel;
import us.cuatoi.s34j.sbs.core.store.model.ConfigurationRepository;
import us.cuatoi.s34j.sbs.core.store.nio.NioConfiguration;
import us.cuatoi.s34j.sbs.core.store.sardine.SardineConfiguration;
import us.cuatoi.s34j.test.TestHelper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.upplication.s3fs.AmazonS3Factory.*;
import static java.nio.file.Files.createTempDirectory;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class TestConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(TestConfigurator.class);

    @Autowired
    private AvailabilityUpdater availabilityUpdater;
    @Autowired
    private ConfigurationRepository configurationRepository;
    @Value("${test.sardine.url:}")
    private String webDavUrl;
    @Value("${test.sardine.user:}")
    private String webDavUser;
    @Value("${test.sardine.password:}")
    private String webDavPassword;
    private Path tempDirectory;

    @PostConstruct
    void start() throws IOException, InvalidPortException, InvalidEndpointException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, ErrorResponseException, NoResponseException, InvalidBucketNameException, XmlPullParserException, InternalException, RegionConflictException {
        logger.info("start()");
        tempDirectory = createTempDirectory("sbs-test-");
        logger.info("stop() Created tempDirectory " + tempDirectory);
        String nioTempFolder = tempDirectory.toUri().toString();
        Files.createDirectory(tempDirectory.resolve("nio-1"));
        createStore("nio-1", "nio", nioTempFolder + "nio-1/");
        Files.createDirectory(tempDirectory.resolve("nio-2"));
        createStore("nio-2", "nio", nioTempFolder + "nio-2/");

        createStore("vfs-1", "vfs", "ram://vfs-1/");
        VFS.getManager().resolveFile(URI.create("ram://vfs-1/")).createFolder();
        createStore("vfs-2", "vfs", "tmp://vfs-2/");
        VFS.getManager().resolveFile(URI.create("tmp://vfs-2/")).createFolder();
        createStore("vfs-3", "vfs", "ram://vfs-3/");
        VFS.getManager().resolveFile(URI.create("ram://vfs-3/")).createFolder();

        if (isNotBlank(webDavUrl)) {
            String url = webDavUrl + "/sardine-1/";
            Sardine sardine = SardineFactory.begin(webDavUser, webDavPassword);
            if (!sardine.exists(url)) {
                sardine.createDirectory(url);
            }
            createStore("sardine", "sardine", url,
                    new SardineConfiguration().setUser(webDavUser).setPassword(webDavPassword));


            //Test minio s3 store
            String minioKey = TestHelper.DEFAULT_KEY;
            String minioSecret = TestHelper.DEFAULT_SECRET;
            String minioHost = "play.minio.io:9000";
            String minioBucket = "sbs-test";
            MinioClient client = new MinioClient("https://" + minioHost, minioKey, minioSecret);
            if (!client.bucketExists(minioBucket)) {
                logger.info("start() Created bucket " + minioBucket);
                client.makeBucket(minioBucket);
            }
            NioConfiguration config = new NioConfiguration();
            config.put(ACCESS_KEY, minioKey);
            config.put(SECRET_KEY, minioSecret);
            config.put(SIGNER_OVERRIDE, "AWSS3V4SignerType");
            config.put(PATH_STYLE_ACCESS, "true");
            config.put("totalBytes", String.valueOf(1024L * 1024 * 1024));
            createStore("nio-s3-1", "nio", "s3://" + minioHost + "/" + minioBucket + "/test/", config);
        }

        availabilityUpdater.updateAll();
    }


    private void createStore(String name, String type, String uri) {
        createStore(name, type, uri, null);
    }

    private void createStore(String name, String type, String uri, Object config) {
        ConfigurationModel store = new ConfigurationModel();
        store.setName(name);
        store.setType(type);
        store.setUri(uri);
        store.setJson(new Gson().toJson(config));
        configurationRepository.save(store);
        logger.info("createStore() name=" + name);
        logger.info("createStore() uri=" + store.getUri());
    }


    @PreDestroy
    void stop() {
        configurationRepository.deleteAll();
        logger.info("stop() Delete tempDirectory " + tempDirectory);
        FileSystemUtils.deleteRecursively(tempDirectory.toFile());
    }
}
