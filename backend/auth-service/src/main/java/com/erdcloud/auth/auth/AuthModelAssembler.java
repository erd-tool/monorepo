package com.erdcloud.auth.auth;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class AuthModelAssembler implements RepresentationModelAssembler<AuthDtos.MeResponse, EntityModel<AuthDtos.MeResponse>> {

    @Override
    public EntityModel<AuthDtos.MeResponse> toModel(AuthDtos.MeResponse response) {
        return EntityModel.of(
            response,
            linkTo(methodOn(AuthController.class).me(null)).withSelfRel()
        );
    }
}
