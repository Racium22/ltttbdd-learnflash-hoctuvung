package com.example.learnflash.giaoDien.manHinhChinh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.learnflash.duLieu.local.thucThe.DanhMuc

// Thành phần giao diện màn hình chính hiển thị danh mục dạng lưới 2 cột
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManHinhChinhUI(
    viewModel: ManHinhChinhViewModel,
    chuyenHuongDanhSachTu: (String, String) -> Unit,
    chuyenHuongCaiDat: () -> Unit,
    chuyenHuongDanhMuc: () -> Unit
) {
    // Thu thập State từ ViewModel
    val danhSachDanhMuc by viewModel.danhSachDanhMuc.collectAsState()
    val thongKe by viewModel.thongKeTuVung.collectAsState()
    val dangKhoiTao by viewModel.dangKhoiTao.collectAsState()
    val loiKhoiTao by viewModel.loiKhoiTao.collectAsState()

    // Khởi tạo SnackbarHostState để hiển thị thông báo lỗi kết nối Firebase
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiển thị SnackBar khi có thông báo lỗi từ Firebase
    LaunchedEffect(loiKhoiTao) {
        if (loiKhoiTao.isNotEmpty()) {
            snackbarHostState.showSnackbar(loiKhoiTao)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Khối tiêu đề gồm tên ứng dụng và dòng thống kê tổng quan
                    Column {
                        // Tên ứng dụng — hiển thị nổi bật ở dòng chính
                        Text(
                            text = "LearnFlash",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        // Dòng phụ: tổng số từ và số từ đã thuộc — hiển thị khi đã có dữ liệu
                        if (!dangKhoiTao && thongKe.first > 0) {
                            val tiLe = if (thongKe.first > 0) thongKe.second * 100 / thongKe.first else 0
                            Text(
                                text = "${thongKe.first} từ · ${thongKe.second} đã thuộc · $tiLe%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Nút điều hướng sang màn hình Quản lý Danh mục kèm label
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { chuyenHuongDanhMuc() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Quản lý danh mục",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Danh mục",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Nút điều hướng sang màn hình Cài đặt kèm label
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { chuyenHuongCaiDat() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Cài đặt",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        when {
            // Trạng thái đang tải dữ liệu Firebase lần đầu — hiển thị Loading indicator
            dangKhoiTao -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Vòng xoay tiến trình trong khi tải dữ liệu từ Firestore
                        CircularProgressIndicator(
                            modifier = Modifier.size(56.dp),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Đang tải dữ liệu...",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Vui lòng đợi trong giây lát",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Trạng thái tải xong — hiển thị lưới danh mục
            else -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    // Thanh tiến trình tổng quan tỉ lệ từ đã thuộc — hiển thị phía dưới TopAppBar
                    if (thongKe.first > 0) {
                        LinearProgressIndicator(
                            progress = { thongKe.second.toFloat() / thongKe.first.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    if (danhSachDanhMuc.isEmpty()) {
                        // Trạng thái trống — Firebase không có dữ liệu hoặc chưa import
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Chưa có dữ liệu",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Kiểm tra kết nối và thử lại",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // Nút thử tải lại khi gặp lỗi kết nối
                                Button(onClick = { viewModel.taiLai() }) {
                                    Text("Thử lại")
                                }
                            }
                        }
                    } else {
                        // Lưới 2 cột LazyVerticalGrid hiển thị các ô danh mục
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(danhSachDanhMuc) { danhMuc ->
                                // Ô danh mục — nhấn để vào danh sách từ của danh mục đó
                                ODanhMuc(
                                    danhMuc = danhMuc,
                                    onClick = { chuyenHuongDanhSachTu(danhMuc.id, danhMuc.ten) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Thành phần Composable hiển thị một ô danh mục trong lưới
@Composable
fun ODanhMuc(danhMuc: DanhMuc, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(12.dp)
            ) {
                // Tên danh mục — hiển thị tối đa 2 dòng, căn giữa
                Text(
                    text = danhMuc.ten,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}