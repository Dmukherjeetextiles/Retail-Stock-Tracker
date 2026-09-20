package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.UserRole

class Converters {
  @TypeConverter
  fun fromUserRole(role: UserRole): String {
    return role.name
  }

  @TypeConverter
  fun toUserRole(value: String): UserRole {
    return try {
      UserRole.valueOf(value)
    } catch (e: Exception) {
      UserRole.EMPLOYEE
    }
  }
}
