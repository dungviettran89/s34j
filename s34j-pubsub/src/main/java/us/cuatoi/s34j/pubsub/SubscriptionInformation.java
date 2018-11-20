package us.cuatoi.s34j.pubsub;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;

public class SubscriptionInformation {
    final Subscriber subscriber;
    final ProjectSubscriptionName subscriptionName;
     final String topic;
    boolean autoRemove = false;

    public SubscriptionInformation(Subscriber subscriber, String topic, ProjectSubscriptionName subscriptionName) {
        this.subscriber = subscriber;
        this.subscriptionName = subscriptionName;
        this.topic = topic;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public boolean isAutoRemove() {
        return autoRemove;
    }



    public SubscriptionInformation autoRemove() {
        this.autoRemove = true;
        return this;
    }
}
