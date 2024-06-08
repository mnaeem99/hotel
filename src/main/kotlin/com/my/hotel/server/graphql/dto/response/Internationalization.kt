package com.my.hotel.server.graphql.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class Internationalization (
    @JsonProperty("detail_title")
    val detailTitle:String?,
    @JsonProperty("detail_main_para")
    val detailMainPara:String?,
    @JsonProperty("detail_description_section1_part1")
    val detailDescriptionSection1Part1:String?,
    @JsonProperty("detail_description_section1_part2")
    val detailDescriptionSection1Part2:String?,
    @JsonProperty("detail_description_section2_part1")
    val detailDescriptionSection2Part1:String?,
    @JsonProperty("detail_description_section2_part2")
    val detailDescriptionSection2Part2: String?,
    @JsonProperty("detail_description_section3_part1")
    val detailDescriptionSection3Part1:String?,
    @JsonProperty("detail_description_section3_part2")
    val detailDescriptionSection3Part2:String?,
    @JsonProperty("featured_countries")
    val featuredCountries:String?,
    @JsonProperty("popular_searches")
    val popularSearches:String?,
    @JsonProperty("neighbourhood")
    val neighbourhood:String?,
    @JsonProperty("header_left_link")
    val headerLeftLink: String?,
    @JsonProperty("header_left_title_link")
    val headerLeftTitleLink: String?,
    @JsonProperty("logo_title")
    val logoTitle: String?,
    @JsonProperty("sign_in")
    val signIn: String?,
    @JsonProperty("sign_up")
    val signUp: String?,
    @JsonProperty("home_title")
    val homeTitle: String?,
    @JsonProperty("home_title_city")
    val homeTitleCity: String?,
    @JsonProperty("home_title_last")
    val homeTitleLast: String?,
    @JsonProperty("home_paragraph_one")
    val homeParagraphOne: String?,
    @JsonProperty("home_paragraph_two")
    val homeParagraphTwo: String?,
    @JsonProperty("our_goal_title")
    val ourGoalTitle: String?,
    @JsonProperty("goal_card_one_title")
    val goalCardOneTitle: String?,
    @JsonProperty("goal_card_one_paragraph")
    val goalCardOneParagraph: String?,
    @JsonProperty("goal_card_two_title")
    val goalCardTwoTitle: String?,
    @JsonProperty("goal_card_two_paragraph")
    val goalCardTwoParagraph: String?,
    @JsonProperty("trending_hotels_title")
    val trendinghotelsTitle: String?,
    @JsonProperty("tab_section_title")
    val tabSectionTitle: String?,
    @JsonProperty("tab_section_title_btn_one")
    val tabSectionTitleBtnOne: String?,
    @JsonProperty("tab_section_title_btn_two")
    val tabSectionTitleBtnTwo: String?,
    @JsonProperty("footer_title")
    val footerTitle: String?,
    @JsonProperty("footer_paragraph")
    val footerParagraph: String?,
    @JsonProperty("footer_btn_one")
    val footerBtnOne: String?,
    @JsonProperty("footer_btn_two")
    val footerBtnTwo: String?,
    @JsonProperty("footer_link_one_paragraph")
    val footerLinkOneParagraph: String?,
    @JsonProperty("footer_link_two_title")
    val footerLinkTwoTitle: String?,
    @JsonProperty("footer_link_two_para")
    val footerLinkTwoPara: String?,
    @JsonProperty("footer_link_three_title")
    val footerLinkThreeTitle: String?,
    @JsonProperty("footer_link_three_para_one")
    val footerLinkThreeParaOne: String?,
    @JsonProperty("footer_link_three_para_two")
    val footerLinkThreeParaTwo: String?,
    @JsonProperty("footer_link_three_para_three")
    val footerLinkThreeParaThree: String?,
    @JsonProperty("footer_link_four_title")
    val footerLinkFourTitle: String?,
    @JsonProperty("footer_link_four_para_one")
    val footerLinkFourParaOne: String?,
    @JsonProperty("footer_link_four_para_two")
    val footerLinkFourParaTwo: String?,
    @JsonProperty("footer_copy_right")
    val footerCopyRight: String?,
    @JsonProperty("swipper_one_title")
    val swipperOneTitle: String?,
    @JsonProperty("swipper_one_para")
    val swipperOnePara: String?,
    @JsonProperty("swipper_two_title")
    val swipperTwoTitle: String?,
    @JsonProperty("swipper_two_para")
    val swipperTwoPara: String?,
    @JsonProperty("swipper_btn")
    val swipperBtn: String?,
    @JsonProperty("followers_text")
    val followersText: String?,
    @JsonProperty("following_text")
    val followingText: String?,
    @JsonProperty("follow_btn")
    val followBtn: String?,
    @JsonProperty("private_account_title")
    val privateAccountTitle: String?,
    @JsonProperty("already_follow")
    val alreadyFollow: String?,
    @JsonProperty("see_favorites_wishlist")
    val seeFavoritesWishlist: String?,
    @JsonProperty("favorites_title")
    val favoritesTitle: String?,
    @JsonProperty("wishlist_title")
    val wishlistTitle: String?,
    @JsonProperty("user_not_added_title")
    val userNotAddedTitle: String?,
    @JsonProperty("yet_title")
    val yetTitle: String?,
    @JsonProperty("user_not_added_para_one")
    val userNotAddedParaOne: String?,
    @JsonProperty("user_not_added_para_two")
    val userNotAddedParaTwo: String?,
    @JsonProperty("sort_hotel_date_asc")
    val sorthotelDateAsc: String?,
    @JsonProperty("sort_hotel_date_desc")
    val sorthotelDateDesc: String?,
    @JsonProperty("sort_hotel_price_desc")
    val sorthotelPriceDesc: String?,
    @JsonProperty("sort_hotel_price_asc")
    val sorthotelPriceAsc: String?,
    @JsonProperty("hotels_title")
    val hotelsTitle: String?,
    @JsonProperty("view_hotel_btn")
    val viewhotelBtn: String?,
    @JsonProperty("added_place_title")
    val addedPlaceTitle: String?,
    @JsonProperty("map_title")
    val mapTitle: String?,
    @JsonProperty("map_btn")
    val mapBtn: String?,
    @JsonProperty("gifts_title")
    val giftsTitle: String?,
    @JsonProperty("similar_hotels_title")
    val similarhotelsTitle: String?,
)