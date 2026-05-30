package com.example.learnflash.duLieu.remote.firebase

import android.util.Log
import com.example.learnflash.duLieu.local.dao.DanhMucDao
import com.example.learnflash.duLieu.local.dao.TuVungDao
import com.example.learnflash.duLieu.local.thucThe.DanhMuc
import com.example.learnflash.duLieu.local.thucThe.TuVung
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.persistentCacheSettings
import kotlinx.coroutines.tasks.await

// Lớp truy xuất và đồng bộ dữ liệu giữa Firebase Firestore và Room Database
class FirebaseNguonDuLieu(
    private val tuVungDao: TuVungDao,
    private val danhMucDao: DanhMucDao
) {
    // Hằng số định danh Tag ghi log Logcat cho các tác vụ Firebase
    private val LOG_TAG = "LearnFlash_Firebase"

    // Hằng số tên Collection trên Firestore
    private companion object {
        const val COLLECTION_DANH_MUC = "danhMuc"
        const val COLLECTION_TU_VUNG = "tuVung"
    }

    // Khởi tạo Instance Firestore với cấu hình cache offline persistent (API mới nhất)
    private val db = Firebase.firestore.also { instance ->
        val caiDat = firestoreSettings {
            // Cấu hình cache dạng persistent — dữ liệu đọc được ngay cả khi mất mạng
            setLocalCacheSettings(persistentCacheSettings {})
        }
        instance.firestoreSettings = caiDat
    }

    // Kiểm tra Room Database — nếu chưa có dữ liệu thì kéo toàn bộ từ Firestore về
    suspend fun khoiTaoDuLieuMacDinh() {
        try {
            val soTuHienCo = tuVungDao.demTongSoTuVungMacDinh()
            if (soTuHienCo == 0) {
                Log.d(LOG_TAG, "Room trống — bắt đầu tải dữ liệu mặc định từ Firestore")
                taiDanhMucTuFirestore()
                taiTuVungTuFirestore()
                Log.d(LOG_TAG, "Tải dữ liệu mặc định từ Firestore hoàn tất")
            } else {
                Log.d(LOG_TAG, "Room đã có $soTuHienCo từ — bỏ qua bước tải dữ liệu mặc định")
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Lỗi khi kiểm tra hoặc tải dữ liệu mặc định: ${e.message}", e)
        }
    }

    // Tải toàn bộ danh mục từ Firestore và lưu vào Room Database
    private suspend fun taiDanhMucTuFirestore() {
        // Gọi Firestore bất đồng bộ bằng .await() từ kotlinx-coroutines-play-services
        val ketQua = db.collection(COLLECTION_DANH_MUC).get().await()
        // Chuyển đổi từng Document Firestore thành Entity DanhMuc rồi lưu vào Room
        for (doc in ketQua.documents) {
            val danhMuc = DanhMuc(
                id = doc.id,
                ten = doc.getString("ten") ?: "",
                moTa = doc.getString("moTa") ?: "",
                laMacDinh = doc.getBoolean("laMacDinh") ?: true
            )
            if (danhMuc.ten.isNotEmpty()) {
                danhMucDao.themHoacCapNhatDanhMuc(danhMuc)
            }
        }
        Log.d(LOG_TAG, "Đã tải ${ketQua.size()} danh mục từ Firestore")
    }

    // Tải toàn bộ từ vựng từ Firestore và lưu vào Room Database
    private suspend fun taiTuVungTuFirestore() {
        val ketQua = db.collection(COLLECTION_TU_VUNG).get().await()
        // Chuyển đổi từng Document Firestore thành Entity TuVung rồi lưu vào Room
        for (doc in ketQua.documents) {
            val tuKhoa = doc.getString("tuKhoa") ?: ""
            // Bỏ qua Document không có từ khóa hợp lệ
            if (tuKhoa.isEmpty()) continue
            val tuVung = TuVung(
                tuKhoa = tuKhoa,
                nghiaTiengViet = doc.getString("nghiaTiengViet") ?: "",
                phienAm = doc.getString("phienAm") ?: "",
                loaiTu = doc.getString("loaiTu") ?: "",
                danhMucId = doc.getString("danhMucId") ?: "mac_dinh",
                capDoSrs = (doc.getLong("capDoSrs") ?: 0L).toInt(),
                ngayOnTapTiepTheo = doc.getLong("ngayOnTapTiepTheo") ?: System.currentTimeMillis(),
                daThuoc = doc.getBoolean("daThuoc") ?: false
            )
            tuVungDao.themTuVung(tuVung)
        }
        Log.d(LOG_TAG, "Đã tải ${ketQua.size()} từ vựng từ Firestore")
    }

    // Đồng bộ tiến độ SRS của một từ vựng lên Firestore sau mỗi phiên đánh giá
    suspend fun dongBoTienDoSrs(tuVung: TuVung) {
        try {
            val ketQua = db.collection(COLLECTION_TU_VUNG)
                .whereEqualTo("tuKhoa", tuVung.tuKhoa)
                .get()
                .await()

            if (!ketQua.isEmpty) {
                // Chỉ cập nhật 3 trường SRS — không ghi đè toàn bộ Document
                val capNhat = mapOf(
                    "capDoSrs" to tuVung.capDoSrs,
                    "ngayOnTapTiepTheo" to tuVung.ngayOnTapTiepTheo,
                    "daThuoc" to tuVung.daThuoc
                )
                ketQua.documents[0].reference.update(capNhat).await()
                Log.d(LOG_TAG, "Đồng bộ SRS lên Firestore thành công: ${tuVung.tuKhoa}")
            }
        } catch (e: Exception) {
            // Ghi log cảnh báo nhưng không crash app — sẽ đồng bộ lại lần sau
            Log.w(LOG_TAG, "Đồng bộ SRS thất bại (sẽ thử lại): ${e.message}")
        }
    }

    // Đẩy một từ vựng mới do người dùng tạo lên Firestore
    suspend fun themTuVungLenFirestore(tuVung: TuVung) {
        try {
            val duLieu = mapOf(
                "tuKhoa" to tuVung.tuKhoa,
                "nghiaTiengViet" to tuVung.nghiaTiengViet,
                "phienAm" to tuVung.phienAm,
                "loaiTu" to tuVung.loaiTu,
                "danhMucId" to tuVung.danhMucId,
                "capDoSrs" to tuVung.capDoSrs,
                "ngayOnTapTiepTheo" to tuVung.ngayOnTapTiepTheo,
                "daThuoc" to tuVung.daThuoc
            )
            db.collection(COLLECTION_TU_VUNG).add(duLieu).await()
            Log.d(LOG_TAG, "Thêm từ mới lên Firestore thành công: ${tuVung.tuKhoa}")
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Thêm từ lên Firestore thất bại: ${e.message}")
        }
    }
}