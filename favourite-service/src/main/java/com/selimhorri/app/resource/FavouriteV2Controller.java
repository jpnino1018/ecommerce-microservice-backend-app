package com.selimhorri.app.resource;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.service.FavouriteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v2/favourites")
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.new-favourites", havingValue = "true")
public class FavouriteV2Controller {

    @Autowired
    private FavouriteService service;

    @GetMapping
    public List<FavouriteDto> findAll() {
        log.info("*** FavouriteDto List, controller V2; fetch all favourites *");

        List<String> randomAdjectives = List.of("Amazing", "Wonderful", "Fantastic", "Incredible", "Awesome", "Spectacular", "Magnificent", "Stunning", "Breathtaking", "Remarkable");
        return this.service.findAll()
                .stream()
                .map(favourite -> {
                    favourite.getProductDto()
                            .setProductTitle(randomAdjectives.get((int) (Math.random() * randomAdjectives.size())) + " "
                                    + favourite.getProductDto().getProductTitle());
                    return favourite;
                })
                .collect(Collectors.toList());
    }
}
