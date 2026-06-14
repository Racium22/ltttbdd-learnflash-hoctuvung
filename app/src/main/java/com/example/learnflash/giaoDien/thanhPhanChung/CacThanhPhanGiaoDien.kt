package com.example.learnflash.giaoDien.thanhPhanChung

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Thành phần giao diện hiển thị vòng xoay tiến trình khi tải dữ liệu chờ trên 3 giây
@Composable
fun ManHinhChoLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// Thành phần hộp thoại hiển thị yêu cầu xác nhận xóa một đối tượng dữ liệu
@Composable
fun HopThoaiXacNhanXoa(
    tenDoiTuong: String,
    onXacNhan: () -> Unit,
    onHuy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onHuy,
        title = {
            // Tiêu đề của hộp thoại xác nhận
            Text(text = "Xác nhận xóa")
        },
        text = {
            // Nội dung thông báo hiển thị chi tiết tên đối tượng cần xóa
            Text(text = "Xóa \"$tenDoiTuong\"? Thao tác không thể hoàn tác.")
        },
        confirmButton = {
            // Nút bấm thực hiện hành động xác nhận xóa dữ liệu
            Button(onClick = onXacNhan) {
                Text("Xóa")
            }
        },
        dismissButton = {
            // Nút bấm thực hiện hành động hủy bỏ không xóa dữ liệu
            TextButton(onClick = onHuy) {
                Text("Hủy")
            }
        }
    )
}
