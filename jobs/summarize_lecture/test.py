import boto3
import json
import logging
import os
import traceback
from botocore.exceptions import ClientError

# 로깅 설정
logger = logging.getLogger()
logger.setLevel(logging.INFO)

def lambda_handler(event, context):
    """
    S3 파일 다운로드 테스트를 위한 Lambda 함수
    지정된 S3 버킷에서 특정 객체를 다운로드합니다.

    Parameters:
    - event: Lambda 이벤트 객체.
      형식: {
        "bucket": "버킷이름",
        "key": "객체키"
      }
    - context: Lambda 컨텍스트 객체

    Returns:
    - 다운로드 성공 여부와 상세 정보
    """
    # 버킷 및 키 정보
    bucket = event.get('bucket', 'study-aid-lecture-materials')  # 기본값 설정
    key = event.get('key', '01581a9c-ff0f-4da9-9a3f-0f18906c1ccc.pdf')  # 이벤트에서 받지 않으면 비워둠

    # 키가 제공되지 않았다면 에러 반환
    if not key:
        return {
            "statusCode": 400,
            "body": json.dumps({
                "message": "파일 키가 제공되지 않았습니다. 'key' 파라미터를 이벤트에 포함해주세요."
            })
        }

    # 다운로드할 로컬 경로
    local_path = f"/tmp/{os.path.basename(key)}"

    # S3 클라이언트 생성
    s3_client = boto3.client('s3', region_name=os.environ.get('AWS_REGION', 'ap-northeast-2'))

    # 결과 객체 초기화
    result = {
        "success": False,
        "bucket": bucket,
        "key": key,
        "local_path": local_path,
        "file_exists": False,
        "file_size": 0,
        "error": None,
        "identity": None,
        "s3_config": {
            "region": os.environ.get('AWS_REGION', 'ap-northeast-2'),
            "endpoint_url": os.environ.get('AWS_ENDPOINT_URL', None)
        }
    }

    try:
        # # 1. IAM 자격 증명 확인
        # try:
        #     sts_client = boto3.client('sts')
        #     identity = sts_client.get_caller_identity()
        #     result["identity"] = {
        #         "Account": identity["Account"],
        #         "ARN": identity["Arn"]
        #     }
        #     logger.info(f"Lambda 실행 역할: {identity['Arn']}")
        # except Exception as e:
        #     logger.warning(f"자격 증명 확인 실패: {str(e)}")
        #     result["identity_error"] = str(e)

        # # 2. 객체 존재 여부 확인 (HeadObject)
        # try:
        #     head_response = s3_client.head_object(Bucket=bucket, Key=key)
        #     result["file_exists"] = True
        #     result["content_type"] = head_response.get('ContentType')
        #     result["content_length"] = head_response.get('ContentLength')
        #     result["last_modified"] = head_response.get('LastModified').strftime("%Y-%m-%d %H:%M:%S")
        #     logger.info(f"객체 확인 성공: {bucket}/{key} (크기: {result['content_length']} 바이트)")
        # except ClientError as e:
        #     error_code = e.response.get('Error', {}).get('Code')
        #     if error_code == '404':
        #         logger.error(f"객체가 존재하지 않습니다: {bucket}/{key}")
        #         result["error"] = "객체가 존재하지 않습니다"
        #     else:
        #         logger.error(f"객체 확인 실패: {str(e)}")
        #         result["error"] = f"객체 확인 실패: {str(e)}"
        #     return {
        #         "statusCode": 404 if error_code == '404' else 500,
        #         "body": json.dumps(result)
        #     }

        # 3. S3에서 파일 다운로드
        logger.info(f"다운로드 시작: s3://{bucket}/{key} -> {local_path}")
        s3_client.download_file(bucket, key, local_path)

        # 4. 다운로드 결과 확인
        if os.path.exists(local_path):
            file_size = os.path.getsize(local_path)
            result["file_size"] = file_size
            result["success"] = True
            logger.info(f"다운로드 성공: {local_path} (크기: {file_size} 바이트)")

            # 파일 내용 샘플 로깅 (텍스트 파일인 경우)
            if result.get('content_type', '').startswith('text/') or key.endswith(('.txt', '.csv', '.json')):
                try:
                    with open(local_path, 'r') as f:
                        sample = f.read(1000)  # 처음 1000자만 읽기
                    result["content_sample"] = sample
                    logger.info(f"파일 내용 샘플: {sample[:100]}...")
                except Exception as e:
                    logger.warning(f"파일 내용 읽기 실패: {str(e)}")
        else:
            result["error"] = "다운로드는 완료되었으나 로컬 파일이 존재하지 않습니다"
            logger.error(result["error"])

        return {
            "statusCode": 200 if result["success"] else 500,
            "body": json.dumps(result)
        }

    except Exception as e:
        # 상세 에러 로깅
        logger.error(f"다운로드 실패: {str(e)}")
        logger.error(traceback.format_exc())

        # 에러 응답 생성
        error_response = getattr(e, 'response', {})
        error_code = error_response.get('Error', {}).get('Code', 'UnknownError')
        error_message = error_response.get('Error', {}).get('Message', str(e))

        result["error"] = f"{error_code}: {error_message}"
        result["traceback"] = traceback.format_exc()

        return {
            "statusCode": 500,
            "body": json.dumps(result)
        }