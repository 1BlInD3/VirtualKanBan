package com.fusetech.virtualkanban.utils

import java.util.*

class XML {
    fun createXml(datum: String, mennyiseg: Double?, cikk: String, raktarbol: String, polcrol: String?, raktarba: String, polcra: String, dolgkod: String): String{
        val xmlTemplate = ("<msg:Msg xsi:schemaLocation='http://Epicor.com/Message/2.0 http://scshost/schemas/epicor/ScalaMessage.xsd' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:msg='http://Epicor.com/Message/2.0'>" +
                "<msg:Hdr>" +
                "<msg:Sender>" +
                "<msg:Name>03</msg:Name>" +
                "<msg:Subname>{0}</msg:Subname>" +
                "</msg:Sender>" +
                "</msg:Hdr>" +
                "<msg:Body>" +
                "<msg:Req msg-type='Stock Transaction' action='Process'>" +
                "<msg:Dta>" +
                "<dta:StockTransaction xsi:schemaLocation='http://www.scala.net/StockTransaction/1.1 http://scshost/schemas/Scala/1.1/StockTransaction.xsd' xmlns:msg='http://Epicor.com/InternalMessage/1.1' xmlns:dta='http://www.scala.net/StockTransaction/1.1'>" +
                "<dta:Movement>" +
                "<dta:TransDate>" + datum + "</dta:TransDate>" +
                "<dta:Qty>" + mennyiseg + "</dta:Qty>" +
                "<dta:StockCode>" + cikk + "</dta:StockCode>" +
                "<dta:WhCodeFrom>"+ raktarbol +"</dta:WhCodeFrom>" +
                "<dta:BinCodeFrom>" + polcrol?.toUpperCase(Locale.ROOT) + "</dta:BinCodeFrom>" +
                "<dta:WhCodeTo>" + raktarba +"</dta:WhCodeTo>" +
                "<dta:BinCodeTo>" + polcra.toUpperCase(Locale.ROOT) + "</dta:BinCodeTo>" +
                "<dta:Ref>"+ dolgkod.toUpperCase(Locale.ROOT) + "</dta:Ref>" +
                //"<dta:OrdNum>" + MRend.Trim() + "</dta:OrdNum>" +
                "<dta:Note>TM</dta:Note>" +
                "</dta:Movement>" +
                "</dta:StockTransaction>" +
                "</msg:Dta>" +
                "</msg:Req>" +
                "</msg:Body>" +
                "</msg:Msg>")
        return xmlTemplate
    }
}