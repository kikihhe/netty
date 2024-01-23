package org.xiaohe.channel.id;

import java.io.Serializable;

public interface ChannelId extends Serializable, Comparable<ChannelId> {


    String asShortText();


    String asLongText();
}