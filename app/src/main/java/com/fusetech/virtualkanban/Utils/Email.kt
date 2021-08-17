package com.fusetech.virtualkanban.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*
import javax.mail.*
import javax.mail.internet.*

class Email {
    fun sendEmail(emailFrom: String, emailTo: String, subject: String, text: String){
        val props = Properties()
        props["mail.smtp.host"] = "mail.fusetech.hu"
        props["mail.smtp.port"] = "25"
        val session = Session.getInstance(props)
        CoroutineScope(IO).launch {
            try {
                val mimeMessage = MimeMessage(session)
                mimeMessage.setFrom(InternetAddress(emailFrom))
                mimeMessage.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(emailTo, false)
                )
                /*mimeMessage.setRecipients(
                    Message.RecipientType.CC,
                    InternetAddress.parse(emailCC, false)
                )*/
                mimeMessage.setText(text)
                mimeMessage.subject = subject
                mimeMessage.sentDate = Date()

                val smtpTransport = session.getTransport("smtp")
                smtpTransport.connect()
                smtpTransport.sendMessage(mimeMessage, mimeMessage.allRecipients)
                smtpTransport.close()
            } catch (messagingException: MessagingException) {
                messagingException.printStackTrace()
            }
        }
    }
}