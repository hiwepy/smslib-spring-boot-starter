package org.smslib.spring.boot.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.smslib.spring.boot.sms.SmsMain;


public class SmsServ extends HttpServlet {

	private static final long serialVersionUID = -7726230356525037386L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		System.out.println("servlet SmsServ ...");
		
		Boolean flag = SmsMain.sendSms("13387089913", "发送测试啊测试");
		
		PrintWriter out = response.getWriter();
		
		if(flag){
			out.println("Send Successful ");
		}else{
			out.println("Send Failed ....");
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}

}
