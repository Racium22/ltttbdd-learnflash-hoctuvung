package com.example.learnflash.duLieu.remote.api

import com.example.learnflash.duLieu.remote.moHinhReMote.MoHinhDichThuat
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Giao diện (Interface) khai báo phương thức gọi HTTP GET đến MyMemory Translation API
interface DichThuatApi {

    // Gọi endpoint dịch thuật với tham số từ cần dịch và cặp ngôn ngữ nguồn|đích
    @GET("get")
    suspend fun dichVanBan(
        // Tham số chuỗi văn bản cần dịch truyền qua query string
        @Query("q") vanBanNguon: String,
        // Tham số cặp ngôn ngữ: en = tiếng Anh nguồn, vi = tiếng Việt đích
        @Query("langpair") capNgonNgu: String = "en|vi"
    ): Response<MoHinhDichThuat>

    companion object {
        // Địa chỉ URL cơ sở của MyMemory Translation API
        private const val URL_CO_SO = "https://api.mymemory.translated.net/"

        // Khởi tạo Instance Retrofit với GsonConverter để tái sử dụng toàn ứng dụng
        fun khoiTaoApi(): DichThuatApi {
            return Retrofit.Builder()
                .baseUrl(URL_CO_SO)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DichThuatApi::class.java)
        }
    }
}