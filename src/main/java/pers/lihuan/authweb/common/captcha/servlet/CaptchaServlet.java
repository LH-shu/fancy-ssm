package pers.lihuan.authweb.common.captcha.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pers.lihuan.authweb.common.captcha.Captcha;
import pers.lihuan.authweb.common.captcha.GifCaptcha;

/*
 * author : LH 2018-05-19 AM
 */
public class CaptchaServlet extends HttpServlet {
	private static final long serialVersionUID = -90304944339413093L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//获取codeKey
        String codeKey = request.getParameter("codeKey");
        if(codeKey==null||codeKey.trim().isEmpty()){
        	return;
        }
        
        //设置输出图片
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");  
        response.setHeader("Cache-Control", "no-cache");  
        response.setDateHeader("Expires", 0);  
        
        // gif验证码, 宽、高、位数
        Captcha captcha = new GifCaptcha(130,38,5);
        // 存入servletContext
        ServletContext servletContext = request.getSession().getServletContext();
        servletContext.setAttribute("code_"+codeKey, captcha.text().toLowerCase());
        
        //输入图片
        captcha.out(response.getOutputStream());
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
}
