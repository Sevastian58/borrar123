package com.cibertec.aplicacionesmovilesiiantony.service.impl;

import com.cibertec.aplicacionesmovilesiiantony.jwt.CustomerDetailsService;
import com.cibertec.aplicacionesmovilesiiantony.jwt.JwtUtil;
import com.cibertec.aplicacionesmovilesiiantony.model.Rol;
import com.cibertec.aplicacionesmovilesiiantony.model.User;
import com.cibertec.aplicacionesmovilesiiantony.repository.UserRepository;
import com.cibertec.aplicacionesmovilesiiantony.service.abstraccion.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    public AuthenticationManager authenticationManager;
    @Autowired
    private CustomerDetailsService customerDetailsService;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public List<User> listAll() {
        return userRepository.findAll();
    }

    @Override
    public ResponseEntity<?> login(Map<String, String> requestMap) {
        System.out.println("dentro de login");
        System.out.println("email: "+ requestMap.get("email"));
        System.out.println("password: "+ requestMap.get("password"));
        Map<String, String> salida = new HashMap<>();
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );

            if(authentication.isAuthenticated()){
                salida.put("token", jwtUtil.generateToken(customerDetailsService.getUserDetail().getEmail(),
                        customerDetailsService.getUserDetail().getRol().getDescription()));
                salida.put("idUsuario", String.valueOf(customerDetailsService.getUserDetail().getId()));
                return  ResponseEntity.ok(salida);
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return  ResponseEntity.badRequest().body("Credenciales incorrectas");
    }

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        System.out.println("registro interno de un usario");
        try{
            if(validateSignUpMap(requestMap)){
                User user = userRepository.iniciarSesion(requestMap.get("email"));
                if(Objects.isNull(user)){
                    requestMap.put("id", "0");
                    userRepository.save(getUserFromMap(requestMap));
                    return ResponseEntity.ok().body("Usuario registrado");
                }else{
                    return ResponseEntity.badRequest().body("Usuario ya existe");
                }
            }
            else{
                return ResponseEntity.badRequest().body("Datos invalidos");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body("algo salio mal");
    }

    @Override
    public ResponseEntity<?> updatePassword(Map<String, String> requestMap) {
        try{
                User user = userRepository.iniciarSesion(requestMap.get("email"));
                if(!Objects.isNull(user)){
                    if(requestMap.get("password").equals(user.getPassword())){

                        //validamos que exista el key con el nuevo password
                        if(requestMap.containsKey("new_password")){

                            user.setPassword(requestMap.get("new_password"));
                            userRepository.save(user);

                            return ResponseEntity.ok().body("Contraseña actualizada");
                        }else{
                            return ResponseEntity.badRequest().body("Agregar el nuevo password");
                        }
                    }
                    else{
                        return ResponseEntity.badRequest().body("Password incorrecto");
                    }
                }else{
                    return ResponseEntity.badRequest().body("Usuario no existe");
                }


        }catch(Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body("algo salio mal");
    }

    @Override
    public ResponseEntity<?> updateUser(Map<String, String> requestMap) {
        try{
            if(validateSignUpMap(requestMap)){
                User user = userRepository.iniciarSesion(requestMap.get("email"));
                if(!Objects.isNull(user)){
                    userRepository.save(getUserFromMap(requestMap));
                    return ResponseEntity.ok().body("Usuario modificado");
                }else{
                    return ResponseEntity.badRequest().body("Usuario no existe");
                }
            }
            else{
                return ResponseEntity.badRequest().body("Datos invalidos");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body("algo salio mal");
    }

    private boolean validateSignUpMap(Map<String, String> requestMap){
        if( requestMap.containsKey("name") && requestMap.containsKey("last_name")
                && requestMap.containsKey("password") && requestMap.containsKey("email")
                && requestMap.containsKey("rol_id")){
            return true;
        }
        else{
            return false;
        }
    }

    private User getUserFromMap(Map<String, String> requestMap){
        User user = new User();

        user.setId(Long.parseLong(requestMap.get("id")));
        System.out.println("El codigo de usuario es " + user.getId());
        user.setName(requestMap.get("name"));
        user.setLast_name(requestMap.get("last_name"));
        user.setPassword(requestMap.get("password"));
        user.setEmail(requestMap.get("email"));




        Rol rol = new Rol();
        rol.setId(Long.parseLong(requestMap.get("rol_id")));

        /*
        String id_rol_seller="1";

        Rol rol = new Rol();
        rol.setId(Long.parseLong(id_rol_seller));
        rol.setDescription("seller");
        */
        user.setRol(rol);

        return user;
    }


}