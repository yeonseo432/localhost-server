package com.waffle.marketing.mission.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.UUID

@Service
@Profile("prod")
class S3ImageService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    @Value("\${aws.s3.bucket}") private val bucket: String,
    @Value("\${aws.s3.region}") private val region: String,
) {
    private val bucketBaseUrl get() = "https://$bucket.s3.$region.amazonaws.com"

    /**
     * S3 presigned PUT URL과 업로드 완료 후 저장될 이미지 URL을 함께 반환.
     * 프론트엔드는 presignedUrl로 직접 PUT 요청해 이미지를 업로드한다.
     */
    fun generatePresignedPutUrl(
        missionId: Long,
        contentType: String,
    ): Pair<String, String> {
        val objectKey = "missions/$missionId/answer/${UUID.randomUUID()}"
        val putObjectRequest =
            PutObjectRequest
                .builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build()
        val presignRequest =
            PutObjectPresignRequest
                .builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build()
        val presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString()
        val imageUrl = "$bucketBaseUrl/$objectKey"
        return presignedUrl to imageUrl
    }

    /**
     * 매장 INVENTORY 답안 이미지용 presigned PUT URL (미션 생성 전, missionId 불필요).
     * S3 경로: stores/{storeId}/inventory/{UUID}
     */
    fun generateStorePresignedPutUrl(
        storeId: Long,
        contentType: String,
    ): Pair<String, String> {
        val objectKey = "stores/$storeId/inventory/${UUID.randomUUID()}"
        val putObjectRequest =
            PutObjectRequest
                .builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build()
        val presignRequest =
            PutObjectPresignRequest
                .builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build()
        val presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString()
        val imageUrl = "$bucketBaseUrl/$objectKey"
        return presignedUrl to imageUrl
    }

    /** 주어진 URL이 이 버킷 소속이면 S3에서 삭제. 다른 출처면 무시. */
    fun deleteIfOurs(imageUrl: String) {
        if (!imageUrl.startsWith(bucketBaseUrl)) return
        val objectKey = imageUrl.removePrefix("$bucketBaseUrl/")
        runCatching {
            s3Client.deleteObject(
                DeleteObjectRequest
                    .builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build(),
            )
        }
    }
}
