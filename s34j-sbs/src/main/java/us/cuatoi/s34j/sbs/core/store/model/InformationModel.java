package us.cuatoi.s34j.sbs.core.store.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Store the store information, which in turn be used to determine the store status
 */
@Entity
@Table(name = "InformationModel",
        indexes = {
                @Index(name = "i_InformationModel_availableBytes", columnList = "active,availableBytes")
        })
public class InformationModel {
    @Id
    @javax.persistence.Id
    private String name;
    @Indexed
    private boolean active;
    private long usedBytes;
    @Indexed
    private long availableBytes;
    private long latency;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(long usedBytes) {
        this.usedBytes = usedBytes;
    }

    public long getAvailableBytes() {
        return availableBytes;
    }

    public void setAvailableBytes(long availableBytes) {
        this.availableBytes = availableBytes;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
