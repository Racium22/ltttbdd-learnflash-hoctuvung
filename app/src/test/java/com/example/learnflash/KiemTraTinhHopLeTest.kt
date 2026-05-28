package com.example.learnflash

import com.example.learnflash.giaoDien.chiTietTuVung.ChiTietViewModel
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.duLieu.local.thucThe.TuVung
import com.example.learnflash.duLieu.local.dao.TuVungDao
import com.example.learnflash.duLieu.local.dao.LichSuOnTapDao
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import com.example.learnflash.duLieu.remote.api.TuVungApi
import com.example.learnflash.duLieu.remote.moHinhReMote.MoHinhTuVungRemote
import kotlinx.coroutines.flow.Flow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * NHIỆM VỤ: Kiểm tra tính hợp lệ (Validation Testing)
 * Sử dụng Fake Object để test logic của ViewModel mà không cần Mockito.
 */
class KiemTraTinhHopLeTest {

    private lateinit var viewModel: ChiTietViewModel
    
    // Tạo một FakeRepository đơn giản (giả lập các phụ thuộc)
    private val fakeKhoDuLieu = KhoDuLieuTuVung(
        tuVungDao = object : TuVungDao {
            override fun layToanBoTuVung(): Flow<List<TuVung>> = TODO()
            override fun layTuVungCanOnTap(thoiGianHienTai: Long): Flow<List<TuVung>> = TODO()
            override suspend fun themTuVung(tuVung: TuVung) {}
            override suspend fun capNhatTuVung(tuVung: TuVung) {}
            override suspend fun xoaTuVung(tuVung: TuVung) {}
            override suspend fun layTuVungTheoId(id: Int): TuVung? = null
            override fun demTongSoTuVung(): Flow<Int> = TODO()
            override fun demSoTuDaThuoc(): Flow<Int> = TODO()
        },
        lichSuOnTapDao = object : LichSuOnTapDao {
            override fun layToanBoLichSu(): Flow<List<LichSuOnTap>> = TODO()
            override suspend fun themLichSu(lichSu: LichSuOnTap) {}
        },
        tuVungApi = object : TuVungApi {
            override suspend fun traCuuTuVung(tuTraCuu: String): Response<List<MoHinhTuVungRemote>> = TODO()
        }
    )

    @Before
    fun setup() {
        // Khởi tạo ViewModel với id = 0 (chế độ thêm mới)
        viewModel = ChiTietViewModel(fakeKhoDuLieu, 0)
    }

    @Test
    fun `kiem tra tu khoa trong - phai bao loi`() {
        viewModel.capNhatTuKhoa("")
        viewModel.capNhatNghia("Xin chào")
        
        viewModel.luuTuVung { /* Hoàn thành */ }
        
        assertEquals("Từ khóa không được để trống", viewModel.loiNhapLieu.value)
    }

    @Test
    fun `kiem tra tu khoa qua dai - phai bao loi`() {
        // Giả sử giới hạn là 50 ký tự
        val tuKhoaDai = "a".repeat(51)
        viewModel.capNhatTuKhoa(tuKhoaDai)
        viewModel.capNhatNghia("Xin chào")
        
        viewModel.luuTuVung { /* Hoàn thành */ }
        
        assertEquals("Từ khóa không được quá 50 ký tự", viewModel.loiNhapLieu.value)
    }

    @Test
    fun `kiem tra nghia trong - phai bao loi`() {
        viewModel.capNhatTuKhoa("Apple")
        viewModel.capNhatNghia("")
        
        viewModel.luuTuVung { /* Hoàn thành */ }
        
        assertEquals("Ý nghĩa không được để trống", viewModel.loiNhapLieu.value)
    }
}
