package com.example.learnflash.duLieu.khoDuLieu

import com.example.learnflash.duLieu.local.dao.LichSuOnTapDao
import com.example.learnflash.duLieu.local.dao.TuVungDao
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import com.example.learnflash.duLieu.local.thucThe.TuVung
import com.example.learnflash.duLieu.remote.api.DichThuatApi
import com.example.learnflash.duLieu.remote.api.TuVungApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

// Lớp Repository xử lý nghiệp vụ điều phối dữ liệu giữa Local Database và Remote API
class KhoDuLieuTuVung(
    private val tuVungDao: TuVungDao,
    private val lichSuOnTapDao: LichSuOnTapDao,
    private val tuVungApi: TuVungApi,
    // Thêm phụ thuộc DichThuatApi phục vụ tác vụ dịch nghĩa sang tiếng Việt
    private val dichThuatApi: DichThuatApi
) {

    // Lấy toàn bộ từ vựng dưới dạng luồng dữ liệu (Flow) để UI phản ứng với thay đổi
    fun layToanBoTuVung(): Flow<List<TuVung>> = tuVungDao.layToanBoTuVung()

    // Lấy danh sách từ vựng tới hạn ôn tập dựa trên thời gian hệ thống
    fun layTuVungCanOnTap(thoiGianHienTai: Long): Flow<List<TuVung>> =
        tuVungDao.layTuVungCanOnTap(thoiGianHienTai)

    // Truy vấn một từ vựng theo ID để phục vụ màn hình Sửa
    suspend fun layTuVungTheoId(id: Int): TuVung? = tuVungDao.layTuVungTheoId(id)

    // Thực thi thao tác thêm hoặc cập nhật từ vựng vào Room Database
    suspend fun luuTuVung(tuVung: TuVung) {
        if (tuVung.id == 0) {
            tuVungDao.themTuVung(tuVung)
        } else {
            tuVungDao.capNhatTuVung(tuVung)
        }
    }

    // Thực thi thao tác xóa từ vựng khỏi Room Database
    suspend fun xoaTuVung(tuVung: TuVung) = tuVungDao.xoaTuVung(tuVung)

    // Thực thi tra cứu phiên âm + loại từ từ Free Dictionary API và dịch nghĩa từ MyMemory API song song
    suspend fun traCuuVaDich(tuKhoa: String): Result<TuVung> {
        return try {
            // Chạy đồng thời 2 HTTP Request bằng coroutineScope + async để giảm thời gian chờ
            coroutineScope {
                val congViecDichThuat = async {
                    dichThuatApi.dichVanBan(vanBanNguon = tuKhoa)
                }
                val congViecPhienAm = async {
                    tuVungApi.traCuuTuVung(tuKhoa)
                }

                // Thu thập kết quả từ 2 tác vụ bất đồng bộ
                val phanHoiDich = congViecDichThuat.await()
                val phanHoiPhienAm = congViecPhienAm.await()

                // Trích xuất nghĩa tiếng Việt từ phản hồi MyMemory API
                val nghiaTiengViet = if (phanHoiDich.isSuccessful &&
                    phanHoiDich.body()?.maKetQua == 200) {
                    phanHoiDich.body()?.duLieuKetQua?.vanBanDaDich ?: ""
                } else ""

                // Trích xuất phiên âm và loại từ từ phản hồi Free Dictionary API
                val duLieuPhienAm = if (phanHoiPhienAm.isSuccessful &&
                    !phanHoiPhienAm.body().isNullOrEmpty()) {
                    phanHoiPhienAm.body()!![0]
                } else null

                val phienAm = duLieuPhienAm?.phienAm ?: ""
                val loaiTu = duLieuPhienAm?.danhSachNghia?.firstOrNull()?.loaiTu ?: ""

                // Xác thực kết quả — cần có ít nhất nghĩa tiếng Việt mới trả về thành công
                if (nghiaTiengViet.isNotEmpty()) {
                    Result.success(
                        TuVung(
                            tuKhoa = tuKhoa,
                            nghiaTiengViet = nghiaTiengViet,
                            phienAm = phienAm,
                            loaiTu = loaiTu
                        )
                    )
                } else {
                    Result.failure(Exception("Không tìm được bản dịch tiếng Việt cho từ \"$tuKhoa\""))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi kết nối mạng: ${e.message}"))
        }
    }

    // --- Các hàm phục vụ Màn hình Thống kê ---

    // Trả về luồng dữ liệu tổng số từ vựng
    fun demTongSoTuVung(): Flow<Int> = tuVungDao.demTongSoTuVung()

    // Trả về luồng dữ liệu số từ đã thuộc
    fun demSoTuDaThuoc(): Flow<Int> = tuVungDao.demSoTuDaThuoc()

    // Trả về luồng dữ liệu số từ chưa thuộc
    fun demSoTuChuaThuoc(): Flow<Int> = tuVungDao.demSoTuChuaThuoc()

    // Trả về luồng dữ liệu giá trị trung bình cấp độ SRS
    fun tinhTrungBinhCapDoSrs(): Flow<Double?> = tuVungDao.tinhTrungBinhCapDoSrs()

    // Trả về luồng dữ liệu giá trị cấp độ SRS cao nhất
    fun layCaoNhatCapDoSrs(): Flow<Int?> = tuVungDao.layCaoNhatCapDoSrs()

    // Trả về luồng dữ liệu giá trị cấp độ SRS thấp nhất
    fun layThapNhatCapDoSrs(): Flow<Int?> = tuVungDao.layThapNhatCapDoSrs()

    // Trả về luồng dữ liệu số từ vựng cần ôn tập trong ngày hôm nay
    fun demTuVungOnTapHomNay(cuoiNgayHomNay: Long): Flow<Int> =
        tuVungDao.demTuVungOnTapHomNay(cuoiNgayHomNay)

    // Trả về luồng dữ liệu số lượt ôn tập đã hoàn thành trong ngày hôm nay
    fun demSoLuotOnTapHomNay(batDauNgay: Long, cuoiNgay: Long): Flow<Int> =
        lichSuOnTapDao.demSoLuotOnTapTrongNgay(batDauNgay, cuoiNgay)

    // Lấy luồng dữ liệu toàn bộ lịch sử ôn tập
    fun layToanBoLichSu(): Flow<List<LichSuOnTap>> = lichSuOnTapDao.layToanBoLichSu()

    // Ghi nhận một phiên ôn tập mới vào Room Database
    suspend fun themLichSuOnTap(lichSu: LichSuOnTap) = lichSuOnTapDao.themLichSu(lichSu)
}