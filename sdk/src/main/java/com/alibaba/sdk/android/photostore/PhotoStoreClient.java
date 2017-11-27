/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.photostore.api.AddAlbumPhotosRequest;
import com.alibaba.sdk.android.photostore.api.AddAlbumPhotosResponse;
import com.alibaba.sdk.android.photostore.api.CreateAlbumRequest;
import com.alibaba.sdk.android.photostore.api.CreateAlbumResponse;
import com.alibaba.sdk.android.photostore.api.DeleteAlbumsRequest;
import com.alibaba.sdk.android.photostore.api.DeleteAlbumsResponse;
import com.alibaba.sdk.android.photostore.api.DeleteFacesRequest;
import com.alibaba.sdk.android.photostore.api.DeleteFacesResponse;
import com.alibaba.sdk.android.photostore.api.DeletePhotosRequest;
import com.alibaba.sdk.android.photostore.api.DeletePhotosResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotoFacesRequest;
import com.alibaba.sdk.android.photostore.api.GetPhotoFacesResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotoStoreRequest;
import com.alibaba.sdk.android.photostore.api.GetPhotoStoreResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotoTagsRequest;
import com.alibaba.sdk.android.photostore.api.GetPhotoTagsResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotosByMd5sRequest;
import com.alibaba.sdk.android.photostore.api.GetPhotosByMd5sResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotosRequest;
import com.alibaba.sdk.android.photostore.api.GetPhotosResponse;
import com.alibaba.sdk.android.photostore.api.GetQuotaRequest;
import com.alibaba.sdk.android.photostore.api.GetQuotaResponse;
import com.alibaba.sdk.android.photostore.api.GetVideoCoverRequest;
import com.alibaba.sdk.android.photostore.api.GetVideoCoverResponse;
import com.alibaba.sdk.android.photostore.api.InactivatePhotosResponse;
import com.alibaba.sdk.android.photostore.api.FaceSetMeRequest;
import com.alibaba.sdk.android.photostore.api.FaceSetMeResponse;
import com.alibaba.sdk.android.photostore.api.InactivatePhotosRequest;
import com.alibaba.sdk.android.photostore.api.ListAlbumPhotosRequest;
import com.alibaba.sdk.android.photostore.api.ListAlbumPhotosResponse;
import com.alibaba.sdk.android.photostore.api.ListAlbumsRequest;
import com.alibaba.sdk.android.photostore.api.ListAlbumsResponse;
import com.alibaba.sdk.android.photostore.api.ListFacePhotosRequest;
import com.alibaba.sdk.android.photostore.api.ListFacePhotosResponse;
import com.alibaba.sdk.android.photostore.api.ListFacesRequest;
import com.alibaba.sdk.android.photostore.api.ListFacesResponse;
import com.alibaba.sdk.android.photostore.api.ListMomentPhotosRequest;
import com.alibaba.sdk.android.photostore.api.ListMomentPhotosResponse;
import com.alibaba.sdk.android.photostore.api.ListMomentsRequest;
import com.alibaba.sdk.android.photostore.api.ListMomentsResponse;
import com.alibaba.sdk.android.photostore.api.ListPhotosResponse;
import com.alibaba.sdk.android.photostore.api.ListTagPhotosRequest;
import com.alibaba.sdk.android.photostore.api.ListTagPhotosResponse;
import com.alibaba.sdk.android.photostore.api.ListTagsRequest;
import com.alibaba.sdk.android.photostore.api.ListTagsResponse;
import com.alibaba.sdk.android.photostore.api.MergeFacesRequest;
import com.alibaba.sdk.android.photostore.api.MergeFacesResponse;
import com.alibaba.sdk.android.photostore.api.MoveAlbumPhotosRequest;
import com.alibaba.sdk.android.photostore.api.MoveAlbumPhotosResponse;
import com.alibaba.sdk.android.photostore.api.MoveFacePhotosRequest;
import com.alibaba.sdk.android.photostore.api.MoveFacePhotosResponse;
import com.alibaba.sdk.android.photostore.api.ReactivatePhotosRequest;
import com.alibaba.sdk.android.photostore.api.ReactivatePhotosResponse;
import com.alibaba.sdk.android.photostore.api.RemoveAlbumPhotosRequest;
import com.alibaba.sdk.android.photostore.api.RemoveAlbumPhotosResponse;
import com.alibaba.sdk.android.photostore.api.RemoveFacePhotosRequest;
import com.alibaba.sdk.android.photostore.api.RemoveFacePhotosResponse;
import com.alibaba.sdk.android.photostore.api.RenameAlbumRequest;
import com.alibaba.sdk.android.photostore.api.RenameAlbumResponse;
import com.alibaba.sdk.android.photostore.api.RenameFaceRequest;
import com.alibaba.sdk.android.photostore.api.RenameFaceResponse;
import com.alibaba.sdk.android.photostore.api.SearchPhotosRequest;
import com.alibaba.sdk.android.photostore.api.SearchPhotosResponse;
import com.alibaba.sdk.android.photostore.api.SetAlbumCoverRequest;
import com.alibaba.sdk.android.photostore.api.SetAlbumCoverResponse;
import com.alibaba.sdk.android.photostore.api.SetFaceCoverRequest;
import com.alibaba.sdk.android.photostore.api.SetFaceCoverResponse;
import com.alibaba.sdk.android.photostore.api.SetQuotaRequest;
import com.alibaba.sdk.android.photostore.api.TransferDelegate;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.CreatePhotoRequest;
import com.alibaba.sdk.android.photostore.api.CreatePhotoResponse;
import com.alibaba.sdk.android.photostore.api.ListPhotosRequest;
import com.alibaba.sdk.android.photostore.api.OpenTransactionRequest;
import com.alibaba.sdk.android.photostore.api.OpenTransactionResponse;
import com.alibaba.sdk.android.photostore.api.GetDownloadRequest;
import com.alibaba.sdk.android.photostore.api.GetDownloadResponse;
import com.alibaba.sdk.android.photostore.api.GetThumbnailRequest;
import com.alibaba.sdk.android.photostore.api.GetThumbnailResponse;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore.runner.NewPagedRequestTask;
import com.alibaba.sdk.android.photostore.runner.PagedRequestTask;
import com.alibaba.sdk.android.photostore.runner.RequestTask;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoStoreClient implements PhotoStore {

    String TAG = PhotoStoreClient.class.getSimpleName();

    public static final String REGION_CN = "cn-shanghai";

    private ExecutorService executorService;

    private IAcsClient client;
    static {
        //设置客户端超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "1000");
        System.setProperty("sun.net.client.defaultReadTimeout", "1000");
        try {
            DefaultProfile.addEndpoint(REGION_CN, REGION_CN, "CloudPhoto", "cloudphoto.cn-shanghai.aliyuncs.com"); //添加自定义endpoint。
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    private String stsToken;
    private String libraryId;
    private String storeName = "cps-demo";
    private boolean bLogin = true;
    private static PhotoStoreClient sInstance;

    public static PhotoStoreClient getInstance() {
        if (sInstance == null) {
            synchronized (PhotoStoreClient.class) {
                sInstance = new PhotoStoreClient();
            }
        }
        return sInstance;
    }

    public void setEnv(String env) {
        String domain = "cloudphoto.cn-shanghai.aliyuncs.com";
        try {
            if (env.equals("pre_release")) {
                domain = "cloudphoto-pre.cn-shanghai.aliyuncs.com";
            }
            DefaultProfile.addEndpoint(REGION_CN, REGION_CN, "CloudPhoto", domain); //添加自定义endpoint。
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    public void setToken(String stsToken) {
        this.stsToken = stsToken;
        this.libraryId = "";
        this.storeName = "cps-demo";
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
        this.stsToken = "";
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setSecurityInfo(String accessKeyId, String secret) {
        IClientProfile profile = DefaultProfile.getProfile(REGION_CN, accessKeyId, secret);
        client = new DefaultAcsClient(profile);
        bLogin = true;
    }

    public boolean isLogin() {
        return bLogin;
    }

    public void logout() {
        bLogin = false;
    }

    private PhotoStoreClient() {
        executorService = Executors.newFixedThreadPool(Constants.DEFAULT_THREAD_POOL_SIZE);
    }

    public void cancelAll() {
        executorService.shutdown();
        executorService = Executors.newFixedThreadPool(Constants.DEFAULT_THREAD_POOL_SIZE);
    }

    public RequestTask openTransaction(long fileSize, String ext, boolean force, String md5, final Callback<OpenTransactionResponse> callback) {
        final OpenTransactionRequest request = new OpenTransactionRequest(fileSize, ext, force, md5);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask createPhoto(String fileId, String sid, String uploadType, String title, final Callback<CreatePhotoResponse> callback) {
        Long shareExpireTime = 0L;
        final CreatePhotoRequest request = new CreatePhotoRequest(fileId, sid, uploadType, title, "remark", shareExpireTime);
        Log.d(TAG, "create photo: " + title);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask getPhotos(List<Long> ids, final Callback<GetPhotosResponse> callback) {
        final GetPhotosRequest request = new GetPhotosRequest(ids);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask getPhotosByMd5s(List<String> md5s, String state, final Callback<GetPhotosByMd5sResponse> callback) {
        final GetPhotosByMd5sRequest request = new GetPhotosByMd5sRequest(md5s, state);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask getPhotoStore(final Callback<GetPhotoStoreResponse> callback) {
        final GetPhotoStoreRequest request = new GetPhotoStoreRequest();
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask getPhotoFaces(Long id, final Callback<GetPhotoFacesResponse> callback) {
        final GetPhotoFacesRequest request = new GetPhotoFacesRequest(id);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask getPhotoTags(Long id, final Callback<GetPhotoTagsResponse> callback) {
        final GetPhotoTagsRequest request = new GetPhotoTagsRequest(id);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public NewPagedRequestTask listPhoto(int size, String cursor, String direction, String state, final Callback<ListPhotosResponse> callback) {
        final ListPhotosRequest request = new ListPhotosRequest(size, cursor, direction, state);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        NewPagedRequestTask task = new NewPagedRequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public NewPagedRequestTask listMoment(int size, String cursor, String direction, String state, final Callback<ListMomentsResponse> callback) {
        final ListMomentsRequest request = new ListMomentsRequest(size, cursor, direction, state);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        NewPagedRequestTask task = new NewPagedRequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public NewPagedRequestTask listMomentPhotos(long momentId, int size, String cursor, String direction, String state, final Callback<ListMomentPhotosResponse> callback) {
        final ListMomentPhotosRequest request = new ListMomentPhotosRequest(momentId, size, cursor, direction, state);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        NewPagedRequestTask task = new NewPagedRequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask listTag(final Callback<ListTagsResponse> callback) {
        final ListTagsRequest request = new ListTagsRequest();
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public NewPagedRequestTask listTagPhotos(long tagId, int size, String cursor, String state, final Callback<ListTagPhotosResponse> callback) {
        final ListTagPhotosRequest request = new ListTagPhotosRequest(tagId, size, cursor, "forward", state);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        NewPagedRequestTask task = new NewPagedRequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public NewPagedRequestTask listFace(int size, String cursor, String direction, String state, boolean hasName, final Callback<ListFacesResponse> callback) {
        final ListFacesRequest request = new ListFacesRequest(size, cursor, direction, state, hasName);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        NewPagedRequestTask task = new NewPagedRequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public NewPagedRequestTask listFacePhotos(long faceId, int size, String cursor, String direction, String state, final Callback<ListFacePhotosResponse> callback) {
        final ListFacePhotosRequest request = new ListFacePhotosRequest(faceId, size, cursor, direction, state);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        NewPagedRequestTask task = new NewPagedRequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask faceSetMe(long id, final Callback<FaceSetMeResponse> callback) {
        final FaceSetMeRequest request = new FaceSetMeRequest(id);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask renameFace(long id, String name, final Callback<RenameFaceResponse> callback) {
        final RenameFaceRequest request = new RenameFaceRequest(id, name);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask deleteFaces(List<Long> ids, final Callback<DeleteFacesResponse> callback) {
        final DeleteFacesRequest request = new DeleteFacesRequest(ids);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask mergeFaces(Long targetId, List<Long> faceIds, final Callback<MergeFacesResponse> callback) {
        final MergeFacesRequest request = new MergeFacesRequest(targetId, faceIds);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask removeFacePhotos(Long id, List<Long> photoIds, final Callback<RemoveFacePhotosResponse> callback) {
        final RemoveFacePhotosRequest request = new RemoveFacePhotosRequest(id, photoIds);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask moveFacePhotos(Long id, List<Long> photoIds, Long targetId, final Callback<MoveFacePhotosResponse> callback) {
        final MoveFacePhotosRequest request = new MoveFacePhotosRequest(id, photoIds, targetId);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask setFaceCover(long faceId, long photoId, final Callback<SetFaceCoverResponse> callback) {
        final SetFaceCoverRequest request = new SetFaceCoverRequest(faceId, photoId);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask createAlbum(String name, final Callback<CreateAlbumResponse> callback) {
        final CreateAlbumRequest request = new CreateAlbumRequest(name, "remark");
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public NewPagedRequestTask listAlbums(int size, String cursor, String direction, String state, final Callback<ListAlbumsResponse> callback) {
        final ListAlbumsRequest request = new ListAlbumsRequest(size, cursor, direction, state);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        NewPagedRequestTask task = new NewPagedRequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public NewPagedRequestTask listAlbumPhotos(long albumId, int size, String cursor, String direction, String state, final Callback<ListAlbumPhotosResponse> callback) {
        final ListAlbumPhotosRequest request = new ListAlbumPhotosRequest(albumId, size, cursor, direction, state);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        NewPagedRequestTask task = new NewPagedRequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask renameAlbum(long albumId, String name, final Callback<RenameAlbumResponse> callback) {
        final RenameAlbumRequest request = new RenameAlbumRequest(albumId, name);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask deleteAlbums(List<Long> ids, final Callback<DeleteAlbumsResponse> callback) {
        final DeleteAlbumsRequest request  = new DeleteAlbumsRequest(ids);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask addAlbumPhotos(long albumId, List<Long> photoIds, final Callback<AddAlbumPhotosResponse> callback) {
        final AddAlbumPhotosRequest request = new AddAlbumPhotosRequest(albumId, photoIds);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask removeAlbumPhotos(long albumId, List<Long> photoIds, final Callback<RemoveAlbumPhotosResponse> callback) {
        final RemoveAlbumPhotosRequest request = new RemoveAlbumPhotosRequest(albumId, photoIds);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask moveAlbumPhotos(long albumId, List<Long> photoIds, long targetId, final Callback<MoveAlbumPhotosResponse> callback) {
        final MoveAlbumPhotosRequest request = new MoveAlbumPhotosRequest(albumId, photoIds, targetId);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask setAlbumCover(long albumId, long photoId, final Callback<SetAlbumCoverResponse> callback) {
        final SetAlbumCoverRequest request = new SetAlbumCoverRequest(albumId, photoId);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask getThumbnail(Long photoId, int width, int height, final Callback<GetThumbnailResponse> callback) {
        String zoomType = "image/resize,m_lfit,w_" + String.valueOf(width) + ",h_" + String.valueOf(height) + "/auto-orient,1";
        final GetThumbnailRequest request = new GetThumbnailRequest(photoId, zoomType);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask getVideoCover(Long photoId, int width, int height, final Callback<GetVideoCoverResponse> callback) {
        String zoomType = "image/resize,m_lfit,w_" + String.valueOf(width) + ",h_" + String.valueOf(height) + "/auto-orient,1";
        final GetVideoCoverRequest request = new GetVideoCoverRequest(photoId, zoomType);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask getDownload(Long photoId, final Callback<GetDownloadResponse> callback) {
        final GetDownloadRequest request = new GetDownloadRequest(photoId);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask inactivePhotos(List<Long> ids, final Callback<InactivatePhotosResponse> callback) {
        final InactivatePhotosRequest request = new InactivatePhotosRequest(ids);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask reactivePhotos(List<Long> ids, final Callback<ReactivatePhotosResponse> callback) {
        final ReactivatePhotosRequest request = new ReactivatePhotosRequest(ids);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask deletePhotos(List<Long> ids, final Callback<DeletePhotosResponse> callback) {
        final DeletePhotosRequest request = new DeletePhotosRequest(ids);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public PagedRequestTask searchPhotos(String keyWord, int page, int size, final Callback<SearchPhotosResponse> callback) {
        final SearchPhotosRequest request = new SearchPhotosRequest(keyWord, page, size);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        PagedRequestTask task = new PagedRequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask getQuota(final Callback<GetQuotaResponse> callback) {
        final GetQuotaRequest request = new GetQuotaRequest();
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public RequestTask setQuota(final long total, final Callback<GetQuotaResponse> callback) {
        final SetQuotaRequest request = new SetQuotaRequest(total);
        request.setStsToken(stsToken);
        request.setLibraryId(libraryId);
        request.setStoreName(storeName);
        RequestTask task = new RequestTask(client, request, callback);
        executorService.execute(task);
        return task;
    }

    public void upload(final Context appContext, final String filePath, long fileSize, String ext, boolean force, String md5, final TransferDelegate callback) {
        final PhotoStoreClient client = PhotoStoreClient.getInstance();
        client.openTransaction(fileSize, ext, force, md5, new Callback<OpenTransactionResponse>() {
            @Override
            public void onSuccess(final OpenTransactionResponse response) {
                Log.d(TAG, String.valueOf(response.code));

                String accessKeyId = response.data.upload.accessKeyId;
                String accessKeySecret = response.data.upload.accessKeySecret;
                String securityToken = response.data.upload.stsToken;
                String endpoint = response.data.upload.ossEndpoint;

                OSSStsTokenCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);

                OSSClient oss = new OSSClient(appContext, endpoint, credentialProvider);


                // 构造上传请求
                String bucket = response.data.upload.bucket;
                String objectKey = response.data.upload.objectKey;
                PutObjectRequest put = new PutObjectRequest(bucket, objectKey, filePath);

                // 异步上传时可以设置进度回调
                put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
                    @Override
                    public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                        Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                        callback.ReportSnapInfo(currentSize, totalSize);
                    }
                });

                OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                    @Override
                    public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                        Log.d("PutObject", "UploadSuccess");
                        String fileId = response.data.upload.fileId;
                        String sid = response.data.upload.sid;
                        File file = new File(filePath);
                        String title = file.getName();
                        client.createPhoto(fileId, sid, "auto", title, new Callback<CreatePhotoResponse>() {
                            @Override
                            public void onSuccess(CreatePhotoResponse response) {
                                callback.onComplete();
                            }

                            @Override
                            public void onFailure(int code, BaseResponse response) {
                                callback.onError(code);
                            }
                        });
                    }

                    @Override
                    public void onFailure(PutObjectRequest request, com.alibaba.sdk.android.oss.ClientException clientExcepion, ServiceException serviceException) {
                        // 请求异常
                        if (clientExcepion != null) {
                            // 本地异常如网络异常等
                            clientExcepion.printStackTrace();
                        }
                        if (serviceException != null) {
                            // 服务异常
                            Log.e("ErrorCode", serviceException.getErrorCode());
                            Log.e("RequestId", serviceException.getRequestId());
                            Log.e("HostId", serviceException.getHostId());
                            Log.e("RawMessage", serviceException.getRawMessage());
                        }
                        callback.onError(-1);
                    }
                });
            }

            @Override
            public void onFailure(int code, BaseResponse response) {
                callback.onError(code);
            }
        });
    }

    // 断点续传，对于移动端来说，如果不是比较大的文件，不建议使用这种方式上传，因为断点续传是通过分片上传实现的，上传单个文件需要进行多次网络请求，效率不高。
    public void resumableUpload(final Context appContext, final String filePath, long fileSize, String ext, boolean force, String md5, final TransferDelegate callback) {
        final PhotoStoreClient client = PhotoStoreClient.getInstance();
        client.openTransaction(fileSize, ext, force, md5, new Callback<OpenTransactionResponse>() {
            @Override
            public void onSuccess(final OpenTransactionResponse response) {
                Log.d(TAG, String.valueOf(response.code));

                String accessKeyId = response.data.upload.accessKeyId;
                String accessKeySecret = response.data.upload.accessKeySecret;
                String securityToken = response.data.upload.stsToken;
                String endpoint = response.data.upload.ossEndpoint;

                OSSStsTokenCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, accessKeySecret, securityToken);

                OSSClient oss = new OSSClient(appContext, endpoint, credentialProvider);


                // 构造上传请求
                String bucket = response.data.upload.bucket;
                String objectKey = response.data.upload.objectKey;

                String recordDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/oss_record/";
                File recordDir = new File(recordDirectory);
                // 要保证目录存在，如果不存在则主动创建
                if (!recordDir.exists()) {
                    recordDir.mkdirs();
                }
                // 创建断点上传请求
                ResumableUploadRequest request = new ResumableUploadRequest(bucket, objectKey, filePath, recordDirectory);
                // 设置上传过程回调
                request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
                    @Override
                    public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                        Log.d("resumableUpload", "currentSize: " + currentSize + " totalSize: " + totalSize);
                    }
                });
                // 异步调用断点上传
                OSSAsyncTask resumableTask = oss.asyncResumableUpload(request, new OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult>() {
                    @Override
                    public void onSuccess(ResumableUploadRequest resumableUploadRequest, ResumableUploadResult resumableUploadResult) {
                        Log.d("resumableUpload", "success!");
                        String fileId = response.data.upload.fileId;
                        String sid = response.data.upload.sid;
                        File file = new File(filePath);
                        String title = file.getName();
                        client.createPhoto(fileId, sid, "auto", title, new Callback<CreatePhotoResponse>() {
                            @Override
                            public void onSuccess(CreatePhotoResponse response) {
                                callback.onComplete();
                            }

                            @Override
                            public void onFailure(int code, BaseResponse response) {
//                                callback.onError(response.code);
                            }
                        });
                    }

                    @Override
                    public void onFailure(ResumableUploadRequest resumableUploadRequest, com.alibaba.sdk.android.oss.ClientException e, ServiceException e1) {
                        // 异常处理
                        if (e != null) {
                            // 本地异常如网络异常等
                            e.printStackTrace();
                        }
                        if (e1 != null) {
                            // 服务异常
                            Log.e("ErrorCode", e1.getErrorCode());
                            Log.e("RequestId", e1.getRequestId());
                            Log.e("HostId", e1.getHostId());
                            Log.e("RawMessage", e1.getRawMessage());
                        }
                        callback.onError(-1);
                    }
                });
            }

            @Override
            public void onFailure(int code, BaseResponse response) {
                callback.onError(code);
            }
        });
    }

}
