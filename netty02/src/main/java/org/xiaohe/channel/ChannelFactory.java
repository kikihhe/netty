package org.xiaohe.channel;

public interface ChannelFactory<T extends Channel> {


    T newChannel();
}