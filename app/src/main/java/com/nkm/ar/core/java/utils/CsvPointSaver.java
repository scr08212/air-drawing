package com.nkm.ar.core.java.utils;

import android.content.Context;
import android.os.Environment; // Environment 임포트
import android.util.Log;

import com.nkm.ar.core.java.datatypes.Point3F;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class CsvPointSaver {

    private static final String TAG = "CsvPointSaver";

    /**
     * 지정된 3D 포인트 목록을 CSV 파일로 사용자의 Downloads 폴더에 저장합니다.
     *
     * @param context 애플리케이션 컨텍스트 (파일 저장 경로를 얻기 위함)
     * @param fileName 저장할 CSV 파일의 이름 (예: "ar_data.csv")
     * @param points 저장할 Point3F 객체의 Vector
     * @return 파일 저장 성공 여부
     */
    public boolean savePointsToCsv(Context context, String fileName, Vector<Point3F> points) {
        if (points == null || points.isEmpty()) {
            Log.w(TAG, "저장할 포인트가 없습니다. 파일이 생성되지 않습니다.");
            return false;
        }

        // 공용 Downloads 디렉토리를 가져옵니다.
        // 참고: API 29+ (Android 10+)부터는 MediaStore 사용이 권장됩니다.
        // 하지만 단순성과 WRITE_EXTERNAL_STORAGE 권한을 통한 광범위한 Android 버전 호환성을 위해 이 방법을 사용합니다.
        // 최신 Android를 타겟팅하는 견고한 앱의 경우 MediaStore API를 고려하세요.
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // 디렉토리가 존재하는지 확인하고 없으면 생성합니다.
        if (!downloadDir.exists()) {
            if (!downloadDir.mkdirs()) { // 상위 디렉토리가 없으면 함께 생성
                Log.e(TAG, "Downloads 디렉토리 생성 실패: " + downloadDir.getAbsolutePath());
                return false;
            }
        }

        File file = new File(downloadDir, fileName);

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);

            // CSV 헤더 작성
            fileWriter.append("X,Y,Z\n");

            // 각 Point3F 객체를 CSV 라인으로 작성
            for (Point3F point : points) {
                fileWriter.append(String.valueOf(point.x)).append(",");
                fileWriter.append(String.valueOf(point.y)).append(",");
                fileWriter.append(String.valueOf(point.z)).append("\n");
            }

            fileWriter.flush();
            Log.d(TAG, "CSV 파일에 성공적으로 포인트 저장: " + file.getAbsolutePath());
            return true;

        } catch (IOException e) {
            Log.e(TAG, "CSV 파일에 포인트 저장 중 오류 발생: " + e.getMessage(), e);
            return false;
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "파일 작성기 닫기 중 오류 발생: " + e.getMessage(), e);
            }
        }
    }
}