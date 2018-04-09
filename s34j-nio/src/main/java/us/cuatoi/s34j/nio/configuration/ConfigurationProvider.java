package us.cuatoi.s34j.nio.configuration;

public interface ConfigurationProvider {
    Storage getStorage(String name);
    Storage getNextStorage(String file);


}
