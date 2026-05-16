package com.ptithcm.waveapp.util;

import android.util.Log;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailHelper {

    private static final String FROM_EMAIL    = "waveappsupport@gmail.com";
    // 💡 LƯU Ý: Hãy đảm bảo bạn đã thay chuỗi dưới này bằng "Mật khẩu ứng dụng" 16 ký tự của Google nhé!
    private static final String FROM_PASSWORD = "wgylvbnekitvbtfu";

    public static boolean sendOtp(String toEmail, String otp) {
        Properties props = new Properties();
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");

        // 🔥 ĐÃ THÊM: Dòng này cực kỳ quan trọng để sửa lỗi kết nối trên Android
        props.put("mail.smtp.ssl.protocols",    "TLSv1.2");

        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Wave Music – Mã xác thực OTP");
            message.setText(
                    "Xin chào!\n\n" +
                            "Mã OTP của bạn là: " + otp + "\n\n" +
                            "Mã có hiệu lực trong 5 phút.\n" +
                            "Vui lòng không chia sẻ mã này.\n\n" +
                            "Wave Music Team"
            );

            // Gửi tin nhắn qua giao thức SMTP bảo mật
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", FROM_EMAIL, FROM_PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            Log.d("EmailHelper", "Đã gửi OTP thành công tới " + toEmail);
            return true;

        } catch (Exception e) {
            // Đã tối ưu hóa việc Log: In ra chi tiết toàn bộ ngăn xếp lỗi (Stack Trace) để bạn dễ theo dõi trong Logcat
            Log.e("EmailHelper", "Lỗi gửi email: " + e.getMessage(), e);
            return false;
        }
    }
}