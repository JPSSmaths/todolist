package com.br.rocketseat.todolist.Filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.br.rocketseat.todolist.user.IUserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var serveletPath = request.getServletPath();
        System.out.println("PATH " + serveletPath);
        if (serveletPath.startsWith("/tasks/")) {
            // Pegar autenticação (usuario e senha)

            var authorization = request.getHeader("Authorization");
            var authEncoded = authorization.substring("Basic".length()).trim();

            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
            String authString = new String(authDecoded);

            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            System.out.println("Authorization");
            System.out.println(username);
            System.out.println(password);
            // validar usuário
            var user = userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401, "Ususário sem altorização");
            } else {
                // validar senha
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

                if (passwordVerify.verified) {
                    // segue viagem
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
            }
        }else{
            filterChain.doFilter(request, response);
        }
    }
}
