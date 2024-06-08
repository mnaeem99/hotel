package com.my.hotel.server.tasks

import com.my.hotel.server.data.model.Admin
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.model.UserAuthentication
import com.my.hotel.server.data.repository.AdminRepository
import com.my.hotel.server.data.repository.MyHotelRepository
import com.my.hotel.server.data.repository.UserAuthenticationRepository
import com.my.hotel.server.data.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Profile("bootstrap")
class DevStartUp @Autowired constructor(
    val userRepository: UserRepository,
    val userAuthenticationRepository: UserAuthenticationRepository,
    val adminRepository: AdminRepository,
    val myHotelRepository: MyHotelRepository,
    var passwordEncoder: PasswordEncoder
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("run dev Start Up Task")

        val appUser = userRepository.save(User("Sami", "Dridi", "s.dridi", "Hello"))
        userAuthenticationRepository.save(UserAuthentication(
            UserAuthentication.Type.EMAIL,
            email = "sami.dridi92@gmail.com",
            password = passwordEncoder.encode("password"),
            user = appUser
        ))

        val testUser = userRepository.save(User("admin", "Admin", "admin", "Hello", dob = LocalDate.of(1995, 1,1)))
        userAuthenticationRepository.save(UserAuthentication(
            UserAuthentication.Type.EMAIL,
            email = "admin@gmail.com",
            password = passwordEncoder.encode("password"),
            user = testUser
        ))

        val users = userRepository.findAll()

        users.forEach {
            it.auths?.let { it1 -> println(it1.size) }
        }

        val admin = Admin("admin", passwordEncoder.encode("password"))
        adminRepository.save(admin)

        //myHotelRepository.save(Hotel("Test", "hello world", "jp", 32F,54F, 2))
    }
}