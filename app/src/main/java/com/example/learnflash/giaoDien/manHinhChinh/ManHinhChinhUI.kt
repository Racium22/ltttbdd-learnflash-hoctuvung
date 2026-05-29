package com.example.learnflash.giaoDien.manHinhChinh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.learnflash.duLieu.local.thucThe.TuVung
import com.example.learnflash.giaoDien.thanhPhanChung.HopThoaiCanhBaoLoi

// Thành phần giao diện tổng quan của màn hình chính
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManHinhChinhUI(
    viewModel: ManHinhChinhViewModel,
    chuyenHuongChiTiet: (Int) -> Unit,
    chuyenHuongCaiDat: () -> Unit,
    chuyenHuongDanhMuc: () -> Unit
) {
    // Thu thập State an toàn từ luồng dữ liệu của ViewModel
    val danhSachTu by viewModel.danhSachTuVung.collectAsState()
    val danhSachDanhMuc by viewModel.danhSachDanhMuc.collectAsState()
    val thongKe by viewModel.thongKeTuVung.collectAsState()
    val tuVungXoa by viewModel.tuVungCanXoa.collectAsState()
    val danhMucDangChon by viewModel.danhMucDangChon.collectAsState()

    // Khung giao diện Scaffold chứa TopAppBar, FAB và danh sách
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LearnFlash") },
                actions = {
                    // Nút điều hướng sang màn hình Danh mục
                    IconButton(onClick = chuyenHuongDanhMuc) {
                        Icon(Icons.Default.Category, contentDescription = "Danh mục")
                    }
                    // Nút điều hướng sang màn hình Cài đặt
                    IconButton(onClick = chuyenHuongCaiDat) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt")
                    }
                }
            )
        },
        floatingActionButton = {
            // Điều hướng sang màn hình Chi Tiết với ID = 0 (Thêm mới)
            FloatingActionButton(onClick = { chuyenHuongChiTiet(0) }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm từ vựng")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Dòng thông tin thống kê tổng quan
            Text(
                text = "Tổng: ${thongKe.first} từ | Đã thuộc: ${thongKe.second}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Thanh cuộn ngang LazyRow hiển thị FilterChip lọc theo danh mục
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chip "Tất cả" — chọn để bỏ lọc danh mục
                item {
                    FilterChip(
                        selected = danhMucDangChon == null,
                        onClick = { viewModel.chonDanhMuc(null) },
                        label = { Text("Tất cả") }
                    )
                }
                // Chip cho từng danh mục trong danh sách
                items(danhSachDanhMuc) { danhMuc ->
                    FilterChip(
                        selected = danhMucDangChon == danhMuc.id,
                        onClick = { viewModel.chonDanhMuc(danhMuc.id) },
                        label = { Text(danhMuc.ten) }
                    )
                }
            }

            // Danh sách cuộn LazyColumn hiển thị các thẻ từ vựng đã lọc
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(danhSachTu) { tuVung ->
                    ItemTuVung(
                        tuVung = tuVung,
                        onSua = { chuyenHuongChiTiet(tuVung.id) },
                        onXoa = { viewModel.yeuCauXoaTuVung(tuVung) }
                    )
                }
            }
        }

        // Hộp thoại xác nhận xóa từ vựng — hiển thị khi tuVungXoa khác null
        if (tuVungXoa != null) {
            HopThoaiCanhBaoLoi(
                tieuDe = "Xác nhận xóa",
                noiDung = "Bạn có chắc chắn muốn xóa từ '${tuVungXoa?.tuKhoa}'?",
                onXacNhan = { viewModel.xacNhanXoaTuVung() }
            )
        }
    }
}

// Thành phần Composable hiển thị một dòng thông tin thẻ từ vựng trong LazyColumn
@Composable
fun ItemTuVung(tuVung: TuVung, onSua: () -> Unit, onXoa: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onSua() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tuVung.tuKhoa, style = MaterialTheme.typography.titleLarge)
                Text(text = tuVung.nghiaTiengViet, style = MaterialTheme.typography.bodyLarge)
                // Hiển thị phiên âm nếu có dữ liệu
                if (tuVung.phienAm.isNotEmpty()) {
                    Text(
                        text = "/${tuVung.phienAm}/",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Nút bấm kích hoạt sự kiện yêu cầu xóa từ vựng lên ViewModel
            IconButton(onClick = onXoa) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa")
            }
        }
    }
}