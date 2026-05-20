package com.example.learnflash.giaoDien.chiTietTuVung

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.learnflash.giaoDien.thanhPhanChung.ManHinhChoLoading

// Thành phần giao diện cấu thành màn hình Thêm và Sửa từ vựng
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChiTietUI(
    viewModel: ChiTietViewModel,
    quayLai: () -> Unit
) {
    // Đọc trạng thái dữ liệu (State) từ ViewModel
    val tuKhoa by viewModel.tuKhoa
    val nghiaTiengViet by viewModel.nghiaTiengViet
    val phienAm by viewModel.phienAm
    val loaiTu by viewModel.loaiTu
    val dangTai by viewModel.dangTai
    val loiNhapLieu by viewModel.loiNhapLieu

    // Khởi tạo SnackbarHostState để hiển thị thông báo lỗi Validation
    val snackbarHostState = remember { SnackbarHostState() }

    // Theo dõi State lỗi và tự động hiển thị SnackBar khi có nội dung lỗi mới
    LaunchedEffect(loiNhapLieu) {
        if (loiNhapLieu.isNotEmpty()) {
            snackbarHostState.showSnackbar(loiNhapLieu)
        }
    }

    // Khung giao diện Scaffold tích hợp TopAppBar và SnackbarHost
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.tuKhoa.value.isEmpty() && !dangTai) "Thêm từ vựng" else "Chỉnh sửa từ vựng") },
                navigationIcon = {
                    // Nút điều hướng quay lại màn hình trước
                    IconButton(onClick = quayLai) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        snackbarHost = {
            // Vùng hiển thị thông báo SnackBar phía dưới màn hình
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Trường nhập liệu từ khóa (bắt buộc)
            OutlinedTextField(
                value = tuKhoa,
                onValueChange = { viewModel.capNhatTuKhoa(it) },
                label = { Text("Từ khóa (Tiếng Anh) *") },
                modifier = Modifier.fillMaxWidth(),
                isError = loiNhapLieu.contains("từ khóa", ignoreCase = true),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Nút kích hoạt tác vụ tra cứu dữ liệu từ Internet (Gọi API)
            Button(
                onClick = { viewModel.traCuuApi() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !dangTai
            ) {
                Text("Tra cứu ý nghĩa trực tuyến (API)")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Trường nhập liệu ý nghĩa tiếng Việt (bắt buộc)
            OutlinedTextField(
                value = nghiaTiengViet,
                onValueChange = { viewModel.capNhatNghia(it) },
                label = { Text("Ý nghĩa (Tiếng Việt) *") },
                modifier = Modifier.fillMaxWidth(),
                isError = loiNhapLieu.contains("ý nghĩa", ignoreCase = true),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Trường nhập liệu phiên âm (tùy chọn, tự động điền từ API)
            OutlinedTextField(
                value = phienAm,
                onValueChange = { viewModel.capNhatPhienAm(it) },
                label = { Text("Phiên âm (tùy chọn)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Trường nhập liệu loại từ (tùy chọn, tự động điền từ API)
            OutlinedTextField(
                value = loaiTu,
                onValueChange = { viewModel.capNhatLoaiTu(it) },
                label = { Text("Loại từ (noun, verb, adj,...)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Nút bấm thực thi lệnh xác thực và lưu dữ liệu
            Button(
                onClick = { viewModel.luuTuVung(hoanThanh = quayLai) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !dangTai
            ) {
                Text("Lưu từ vựng")
            }
        }
    }

    // Hiển thị lớp phủ vòng xoay Loading khi đang tải dữ liệu hoặc gọi API
    if (dangTai) {
        ManHinhChoLoading()
    }
}