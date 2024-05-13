package com.example.shopping_app.ApiHelper

import com.example.shopping_app.Model

class MemberInfoHelper {
    companion object {
        val memberInfoMap: MutableMap<String, String?> = mutableMapOf()
    }

    fun createMemberInfoMap(cName: String, cNickName: String?, cEmail: String, cPhone: String?,
                            cAddr: String?, cCountry: String?, cBirthday: String?, cSex: String?, cImage: String?) {
        memberInfoMap[Model.cName] = cName
        memberInfoMap[Model.cNickName] = cNickName
        memberInfoMap[Model.cEmail] = cEmail
        memberInfoMap[Model.cPhone] = cPhone
        memberInfoMap[Model.cAddr] = cAddr
        memberInfoMap[Model.cCountry] = cCountry
        memberInfoMap[Model.cBirthday] = cBirthday
        memberInfoMap[Model.cSex] = cSex
        if (cImage != null) {
            if(cImage.isNotEmpty()) {
                memberInfoMap[Model.cImage] = cImage
            }
        }
    }
    fun replaceMapString(
        map: MutableMap<String, String?>,
        key: String,
        replaceString: String
    ): String {
        if (map[key].toString() == "Unknown" || map[key].toString() == "null" || map[key].toString() == " ") {
            map[key] = replaceString
        }
        return map[key]!!
    }
    fun makeDateString(day: Int, month: Int): String? {
        return getMonthFormat(month) + "-" + day
    }
    private fun getMonthFormat(month: Int): String? {
        return when(month) {
            1 -> "01"
            2 -> "02"
            3 -> "03"
            4 -> "04"
            5 -> "05"
            6 -> "06"
            7 -> "07"
            8 -> "08"
            9 -> "09"
            10 -> "10"
            11 -> "11"
            12 -> "12"
            else -> "01"
        }
    }
}