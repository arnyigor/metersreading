package com.arny.metersreading.core.models

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class Params(var paramsValue: JSONObject = JSONObject()) {
    private var nodeObject: JSONObject? = null
    private var nodeString: String? = null
    private var nodeArray: JSONArray? = null

    constructor(params: String?) : this(JSONObject()) {
        try {
            if (!params.isNullOrBlank()) {
                this.paramsValue = JSONObject(params)
            } else {
                this.paramsValue = JSONObject()
            }
        } catch (e: JSONException) {
            this.paramsValue = JSONObject()
            e.printStackTrace()
        }
    }

    fun getParam(path: String, defaultVal: String?): String? {
        resetParams()
        nodeObject = paramsValue
        return try {
            parseJsonPath(path)
            nodeString
        } catch (e: JSONException) {
            e.printStackTrace()
            defaultVal
        }
    }

    fun getParam(path: String): String? {
        if (paramsValue.length() == 0) {
            return null
        }
        var result: String? = null
        resetParams()
        nodeObject = paramsValue
        try {
            parseJsonPath(path)
            when {
                nodeObject != null -> {
                    result = nodeObject.toString()
                }
                nodeString != null -> {
                    result = nodeString
                }
                nodeArray != null -> {
                    result = nodeArray.toString()
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return result
    }

    fun setParam(path: String, `val`: Any): Boolean {
        return setParamVal(path, `val`)
    }

    private fun setParamVal(path: String, `val`: Any): Boolean {
        try {
            paramsValue = setProperty(paramsValue, path, `val`)
            return true
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return false
    }

    fun removeParam(path: String): Boolean {
        val jsonObject = paramsValue
        try {
            paramsValue = unsetProperty(jsonObject, path)
            return true
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return false
    }

    @Throws(JSONException::class)
    private fun unsetProperty(obj: JSONObject, keys: String): JSONObject {
        var jsonObject = obj
        val keyMain = keys.split("\\.").toTypedArray()
        for (keym in keyMain) {
            val hasKey = jsonObject.has(keym)
            var key: String
            if (hasKey) {
                val iterator: Iterator<*> = jsonObject.keys()
                while (iterator.hasNext()) {
                    key = iterator.next() as String
                    if (key == keym) {
                        if (keym == keyMain[keyMain.size - 1]) {
                            jsonObject.remove(key)
                            return jsonObject
                        } else if (jsonObject.optJSONObject(key) != null && jsonObject.optJSONArray(
                                key
                            ) == null
                        ) {
                            jsonObject = jsonObject.getJSONObject(key)
                            break
                        } else if (jsonObject.optJSONArray(key) == null) {
                            jsonObject.put(key, JSONObject())
                            jsonObject = jsonObject.getJSONObject(key)
                            break
                        } else if (jsonObject.optJSONArray(key) != null) {
                            jsonObject.put(key, JSONObject())
                            jsonObject = jsonObject.getJSONObject(key)
                        }
                    }
                }
            }
        }
        return jsonObject
    }

    @Throws(JSONException::class)
    private fun setProperty(obj: JSONObject, keys: String, valueNew: Any): JSONObject {
        var jsonObject = obj
        val keyMain = keys.split("\\.").toTypedArray()
        for (keym in keyMain) {
            val hasKey = jsonObject.has(keym)
            var key: String
            if (hasKey) {
                val iterator: Iterator<*> = jsonObject.keys()
                while (iterator.hasNext()) {
                    key = iterator.next() as String
                    if (key == keym) {
                        if (keym == keyMain[keyMain.size - 1]) {
                            jsonObject.put(key, valueNew)
                            return jsonObject
                        } else if (jsonObject.optJSONObject(key) != null && jsonObject.optJSONArray(
                                key
                            ) == null
                        ) {
                            jsonObject = jsonObject.getJSONObject(key)
                            break
                        } else {
                            if (jsonObject.optJSONArray(key) == null) {
                                jsonObject.put(key, JSONObject())
                                jsonObject = jsonObject.getJSONObject(key)
                                break
                            } else if (jsonObject.optJSONArray(key) != null) {
                                jsonObject.put(key, JSONObject())
                                jsonObject = jsonObject.getJSONObject(key)
                            }
                        }
                    }
                }
            } else {
                jsonObject = if (keym != keyMain[keyMain.size - 1]) {
                    jsonObject.put(keym, JSONObject())
                    jsonObject.getJSONObject(keym)
                } else {
                    jsonObject.put(keym, valueNew)
                }
            }
        }
        return jsonObject
    }

    @Throws(JSONException::class)
    private fun parseJsonPath(path: String) {
        var obj: Any
        val pathArr = path.split("\\.").toTypedArray()
        for (pathElem in pathArr) {
            obj = if (nodeObject != null) {
                if (!nodeObject!!.has(pathElem)) {
                    resetParams()
                    return
                }
                nodeObject!![pathElem]
            } else {
                nodeArray!![pathElem.toInt()]
            }
            if (obj is JSONArray) {
                nodeObject = null
                nodeArray = JSONArray(obj.toString())
            } else if (obj is String || obj is Int) {
                nodeObject = null
                nodeArray = null
                nodeString = obj.toString()
            } else if (obj is JSONObject) {
                nodeObject = JSONObject(obj.toString())
                nodeArray = null
            }
        }
    }

    private fun resetParams() {
        nodeArray = null
        nodeObject = null
        nodeString = null
    }

    fun getParam(path: String, defaultVal: JSONObject?): JSONObject? {
        nodeObject = paramsValue
        val result: JSONObject? = try {
            parseJsonPath(path)
            nodeObject
        } catch (e: JSONException) {
            e.printStackTrace()
            defaultVal
        }
        resetParams()
        return result
    }

    fun getParam(path: String, defaultVal: JSONArray?): JSONArray? {
        nodeObject = paramsValue
        val result: JSONArray? = try {
            parseJsonPath(path)
            nodeArray
        } catch (e: JSONException) {
            e.printStackTrace()
            defaultVal
        }
        resetParams()
        return result
    }

    val stringParams: String
        get() = paramsValue.toString()

    fun setParams(params: String?) {
        try {
            if (params != null) {
                this.paramsValue = JSONObject(params)
            }
        } catch (e: JSONException) {
            this.paramsValue = JSONObject()
            e.printStackTrace()
        }
    }

    override fun toString(): String {
        return stringParams
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Params
        if (paramsValue != other.paramsValue) return false
        return true
    }

    override fun hashCode(): Int {
        return paramsValue.hashCode()
    }
}
