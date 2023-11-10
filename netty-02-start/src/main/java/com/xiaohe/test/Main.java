package com.xiaohe.test;


import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-07 21:56
 */
public class Main {
    private static Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeAdapter()).serializeNulls().create();

//    public static void main(String[] args) throws IOException {
//        User user = new User();
//        user.setAge(18);
//        user.setBodyWeight(120);
//        user.setHeight(180);
//        user.setId(10000);
//        user.setName("张三");
//        user.setSchool("XXXXXXXXXXXX");
//
//        // JDK序列化:
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
//        objectOutputStream.writeObject(user);
//        objectOutputStream.flush();
//        objectOutputStream.close();
//        System.out.println("JDK序列化大小: " + byteArrayOutputStream.toByteArray().length);
//        byteArrayOutputStream.close();
//
//        // JSON序列化:
//        System.out.println("JSON序列化大小:" + gson.toJson(user).getBytes().length);
//
//        // Protostuff序列化:
//        System.out.println("ProtoStuff序列化大小: " + ProtoStuffUtils.serialize(user).length);
//
//    }
    public static void main(String[] args) throws IOException {
        User user = new User();
        user.setAge(18);
        user.setBodyWeight(120);
        user.setHeight(180);
        user.setId(10000);
        user.setName("张三");
        user.setSchool("XXXXXXXXXXXX");

        // JDK序列化:
        long jdkStart = System.currentTimeMillis();
        for (int i = 0; i < 3000000; i++) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(user);
            objectOutputStream.flush();
            objectOutputStream.close();

            byte[] bytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        }

        long jdkEnd = System.currentTimeMillis();
        System.out.println("JDK序列化耗时: " + (jdkEnd - jdkStart));


        // JSON序列化:
        long jsonStart = System.currentTimeMillis();
        for (int i = 0; i < 3000000; i++) {
            gson.toJson(user);
        }
        long jsonEnd = System.currentTimeMillis();
        System.out.println("JSON序列化耗时:" + (jsonEnd - jsonStart));

        // Protostuff序列化:
        long protostuffStart = System.currentTimeMillis();
        for (int i = 0; i < 3000000; i++) {
            ProtoStuffUtils.serialize(user);
        }
        long protostuffEnd = System.currentTimeMillis();
        System.out.println("Protostuff耗时: " + (protostuffEnd - protostuffStart));

    }
}
