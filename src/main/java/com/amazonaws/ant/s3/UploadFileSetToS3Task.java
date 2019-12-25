/*

 * Copyright 2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.ant.s3;

import java.io.File;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.ObjectCannedAclProvider;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

/**
 * Ant Task for uploading a fileset or filesets to S3.
 */
public class UploadFileSetToS3Task extends AWSAntTask {
    private Vector<FileSet> filesets = new Vector<FileSet>();
    private String bucketName;
    private String keyPrefix;
    private boolean printStatusUpdates = false;
    private boolean continueOnFail = false;
    private int statusUpdatePeriodInMs = 500;

    /**
     * Specify a fileset to be deployed.
     *
     * @param fileset
     *            A fileset, whose files will all be deployed to S3
     */
    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }

    /**
     * Specify the name of your S3 bucket
     *
     * @param bucketName
     *            The name of the bucket in S3 to store the files in. An
     *            exception will be thrown if it doesn't exist.
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Specify the prefix to your files in this upload. This is optional
     *
     * @param keyPrefix
     *            If specified, all of your files in the fileset will have this
     *            prefixed to their key. For example, you can name this
     *            "myfiles/", and if you upload myfile.txt, its key in S3 will
     *            be myfiles/myfile.txt.
     */
    public void setKeyPrefix(String keyPrefix) {
    	if(keyPrefix==null) {
    		this.keyPrefix = "";
    	}
    	else this.keyPrefix = keyPrefix;
    }

    /**
     * Specify whether you want to continue uploading your fileset if the upload
     * of one file fails. False by default.
     *
     * @param continueOnFail
     *            If true, the task will continue to upload files from the
     *            fileset if any single files fails to upload. Otherwise, one
     *            file
     */
    public void setContinueOnFail(String continueOnFail) {
        this.continueOnFail = new Boolean(continueOnFail);
    }

    /**
     * Specify whether to print updates about your upload. The update will
     * consist of how many bytes have been uploaded versus how many are to be
     * uploaded in total. Not required, default is false.
     *
     * @param printStatusUpdates
     *            Whether you want the task to print status updates about your
     *            upload.
     */
    public void setPrintStatusUpdates(boolean printStatusUpdates) {
        this.printStatusUpdates = printStatusUpdates;
    }

    /**
     * Set how long to wait in between polls of your upload when printing
     * status. Not required, default is 500. Setting will do nothing unless
     * printStatusUpdates is true.
     *
     * @param statusUpdatePeriodInMs
     *            How long to wait in between polls of your upload when printing
     *            status
     */
    public void setStatusUpdatePeriodInMs(int statusUpdatePeriodInMs) {
        this.statusUpdatePeriodInMs = statusUpdatePeriodInMs;
    }

    /**
     * Verifies that all necessary parameters were set
     */
    private void checkParameters() {
        StringBuilder errors = new StringBuilder("");
        boolean areMalformedParams = false;
        if (bucketName == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: bucketName is required \n");
        }
        if (filesets.size() < 1) {
            areMalformedParams = true;
            errors.append("Missing parameter: you must specify at least one fileset \n");
        }
        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }
    }

    /**
     * Uploads files to S3
     */
    @Override
	public void execute() {
        checkParameters();
        TransferManager transferManager;
        
//        AmazonS3ClientBuilder
//				.standard()
//				.withRegion("us-east-1")
//				.withClientConfiguration(new ClientConfiguration().withMaxConnections(100)
//                .withConnectionTimeout(120 * 1000)
//                .withMaxErrorRetry(15))
//				.build();
        
        
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
//                                .withRegion("us-east-1")
                                .withRegion(Regions.US_EAST_1)
                                .build();
//        s3Client.putObject(
//        		  bucketName, 
//        		  "war", 
//        		  
//        		);
        
//        AmazonS3 amazonS3 = AmazonS3ClientBuilder
//        		  .standard()
//        		  .withCredentials(new DefaultAWSCredentialsProviderChain())
//        		  .withRegion(Regions.DEFAULT_REGION)
//        		  .build();
        
        if (awsSecretKey != null && awsAccessKeyId != null) {
//        	transferManager = new TransferManager(AmazonS3ClientBuilder
//    				.standard()
//    				.withRegion("us-east-1")
////    				.withClientConfiguration(new ClientConfiguration().withMaxConnections(100)
////                    .withConnectionTimeout(120 * 1000)
////                    .withMaxErrorRetry(15))
//    				.build());
//            transferManager = new TransferManager(getOrCreateClient(AmazonS3Client.class));
        	transferManager = new TransferManager(s3Client);
        	
        } else {
            transferManager = new TransferManager();
        }
        
//        if (awsSecretKey != null && awsAccessKeyId != null) {
//            transferManager = new TransferManager(getOrCreateClient(AmazonS3Client.class));
//        } else {
//            transferManager = new TransferManager();
//        }
        
        if(keyPrefix==null) {
    		keyPrefix = "";
    	}
        
        
        ObjectCannedAclProvider cannedAclProvider = new ObjectCannedAclProvider() {

            public CannedAccessControlList provideObjectCannedAcl(File file) {
                    return CannedAccessControlList.PublicRead;
            }
        };
    	
        
        
        
        
        
        
        
        
//        TransferManager xfer_mgr = new TransferManager(getOrCreateClient(AmazonS3Client.class));
////        TransferManager xfer_mgr = TransferManagerBuilder.stand	ard().build();
//        
//        
//        try {
//            MultipleFileUpload xfer = xfer_mgr.uploadDirectory(bucketName,
//            		keyPrefix, new File("war"), true);
//            // loop with Transfer.isDone()
//            XferMgrProgress.showTransferProgress(xfer);
//            // or block with Transfer.waitForCompletion()
//            XferMgrProgress.waitForCompletion(xfer);
//        } catch (AmazonServiceException e) {
//            System.err.println(e.getErrorMessage());
//            System.exit(1);
//        }
//        xfer_mgr.shutdownNow();
//        
//        if(true)return;
        
        
        
        
        
        
        for (FileSet fileSet : filesets) {
            DirectoryScanner directoryScanner = fileSet.getDirectoryScanner(getProject());
            String[] includedFiles = directoryScanner.getIncludedFiles();
            try {
                for (String includedFile : includedFiles) {
                    File base = directoryScanner.getBasedir();
                    File file = new File(base, includedFile);
                    
                    String key = keyPrefix + file.getName();
                    
                    String keyBase = keyPrefix + base.getName();
                    
                    
                    
                    
                    
                    
                    
                    
                    try {
//                    	System.out.println("base directory path:"+base.getPath());
//                    	System.out.println("base directory path: "+keyBase+"...");
                    	
                    	
//                    	String path = file.getPath();
//                    	int indexOfWar=path.indexOf("war/");
//                		
//                		path = path.substring(indexOfWar,path.length());
//                		
                		
//                		if(Pattern.matches("geeksforge*ks", 
//                                "geeksforgeeks")) {
//                			
//                		}
                		
//                		if(path.contains("/classes/") || path.contains(".class")) {
//                			System.out.println("Skipping class file " + file.getName()
//                            + "...");
//                			continue;
//                		}
//                		
//                    	if(path.indexOf(''war))
//                    	System.out.println("file path:"+path);
//                        System.out.println("Uploading file " + file.getName()
//                                + "...");
                        
                        
                		
//                		
//                		System.out.println("Uploading file at path " + key
//                        + "...");
                		
                        
//                        Upload upload = transferManager.upload(bucketName, key, file);
//                        Upload upload = transferManager.upload(new PutObjectRequest(bucketName, path, file).withCannedAcl((CannedAccessControlList.PublicRead)));
                        
                    	MultipleFileUpload mulupload = transferManager.uploadDirectory(bucketName, keyBase, base, true, null, null, cannedAclProvider);
                    	mulupload.addProgressListener(new ProgressListener(){
                            public void progressChanged(ProgressEvent progressEvent) {
                                System.out.println("Transferred bytes: " + progressEvent.getBytesTransferred());
                            }
                        });
//                    	ProgressEvent progressEvent = new ProgressEvent;
//                    	ProgressListener progressListener = progressEvent -> System.out.println(
//                    			  "Transferred bytes: " + progressEvent.getBytesTransferred());
//                    	mulupload.addProgressListener(listener);
//                        Upload upload = transferManager.upload(bucketName, key, file);
//                        if (printStatusUpdates) {
//                            while (!upload.isDone()) {
//                                System.out.print(upload.getProgress()
//                                        .getBytesTransferred()
//                                        + "/"
//                                        + upload.getProgress()
//                                                .getTotalBytesToTransfer()
//                                        + " bytes transferred...\r");
//                                Thread.sleep(statusUpdatePeriodInMs);
//                            }
//                            System.out.print(upload.getProgress()
//                                        .getBytesTransferred()
//                                        + "/"
//                                        + upload.getProgress()
//                                                .getTotalBytesToTransfer()
//                                        + " bytes transferred...\n");
//                        } else {
//                            upload.waitForCompletion();
//                        }
                    	XferMgrProgress.showTransferProgress(mulupload);
                      // or block with Transfer.waitForCompletion()
                    	XferMgrProgress.waitForCompletion(mulupload);
                    	mulupload.waitForCompletion();
                        System.out.println("Upload succesful");
                        break;
                    } catch (Exception e) {
                        if (!continueOnFail) {
                            throw new BuildException(
                                    "Error. The file that failed to upload was: "
                                            + file.getName() + ": " + e, e);
                        } else {
                            System.err.println("The file " + file.getName()
                                    + " failed to upload. Continuing...");
                        }
                    }
                }
            } finally {
                transferManager.shutdownNow(false);
            }
        }
    }
}
