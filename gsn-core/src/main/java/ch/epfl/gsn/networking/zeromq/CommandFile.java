package ch.epfl.gsn.networking.zeromq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;

public class CommandFile implements Serializable{
    private String fileKey;
    private String fileName;
    private String contentType;
    private String fileContent;

    public CommandFile() {
        this.fileKey = "";
        this.fileName = "";
        this.contentType = "";
        this.fileContent = "";
    }

    public CommandFile(String fileKey, String fileName, String contentType, String fileContent) {
        this.fileKey = fileKey;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileContent = fileContent;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public FileItem getFileItem(){

        DiskFileItemFactory factory= new DiskFileItemFactory();
        factory.setSizeThreshold(1024*1024); //

         try {
            // Convert file content to InputStream
            InputStream fileContentStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

            // Create DiskFileItem using constructor
            FileItem fileItem = factory.createItem(
                this.fileKey, 
                this.contentType,
                false,
                this.fileName);

            // Set the input stream for the file content
            fileItem.getOutputStream(); // This opens the output stream
            IOUtils.copy(fileContentStream, fileItem.getOutputStream());
            fileContentStream.close();

            return fileItem;
            
            // Now you have a FileItem created from file content and filename
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
