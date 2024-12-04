package org.webapp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class FileUtils {
    public static boolean isImage(MultipartFile file, String userId) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                log.warn("The user: {} tries to upload a non-image avatar file.", userId);
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            log.warn("The user: {} tries to upload a non-image avatar file.", userId);
            return false;
        }
    }

    public static boolean isVideo(String filename) {
        //TODO: 判断文件真实类型
        Set<String> suffix = new HashSet<>(Arrays.asList("mp4", "avi", "flv", "wmv", "mkv", "mpeg"));
        return suffix.contains(filename.substring(filename.lastIndexOf(".") + 1));
    }

    public static String saveAvatar(MultipartFile picture, String userId) {
        String filename = userId + ".jpg";
        File file;
        String environment = System.getProperty("os.name").toLowerCase();
        String userCoverPathOnWindows = "D:\\WebApp\\ServerData\\UserCover";
        String userCoverPathOnLinux = "/usr/local/bin/WebApp/ServerData/UserCover";
        if (environment.contains("windows")) {
            file = new File(userCoverPathOnWindows);
        } else {
            file = new File(userCoverPathOnLinux);
        }
        file.mkdirs();
        if (environment.contains("windows")) {
            file = new File(userCoverPathOnWindows + "\\" + filename);
        } else {
            file = new File(userCoverPathOnLinux + "/" + filename);
        }
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            picture.transferTo(file);
            log.info("The user: {} uploads the avatar successfully.", userId);
            // 文件路径
            if (environment.contains("windows")) {
                return userCoverPathOnWindows + "\\" + filename;
            } else {
                return userCoverPathOnLinux + "/" + filename;
            }
        } catch (IOException e) {
            file.delete();
            log.error("The user: {} fails to upload the avatar.", userId);
            throw new RuntimeException(e);
        }
    }

    public static boolean saveImage(String fromUserId, String toUserId, String image, String imageUrl) {
        File file = new File(imageUrl);
        try {
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(Base64.getDecoder().decode(image));
            return true;
        } catch (Exception e) {
            file.delete();
            log.error("Fail to parse and save the image in the message sent by the user: {} to the user: {}.", fromUserId, toUserId, e);
            return false;
        }
    }
}
