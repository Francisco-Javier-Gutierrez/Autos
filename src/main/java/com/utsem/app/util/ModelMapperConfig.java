package com.utsem.app.util;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class ModelMapperConfig {

	@Bean
	ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
