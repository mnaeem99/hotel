package com.my.hotel.server.service.quality

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.Quality
import com.my.hotel.server.data.model.QualityType
import com.my.hotel.server.data.repository.QualityRepository
import com.my.hotel.server.data.repository.QualityTypeRepository
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.error.AlreadyExistCustomException
import com.my.hotel.server.graphql.error.NotFoundCustomException
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Slf4j
@Validated
class QualityService @Autowired constructor(
    val qualityRepository: QualityRepository,
    val qualityTypeRepository: QualityTypeRepository
): IQualityService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun getQualities(): List<Quality>?{
        return qualityRepository.findAll()
    }
    override fun getQualityTypes(): List<QualityType>?{
        return qualityTypeRepository.findAll()
    }
    override fun getQualities(pageOptions: GraphQLPage): Page<Quality>?{
        return qualityRepository.findAll(pageOptions.toPageable())
    }
    override fun getQualityTypes(pageOptions: GraphQLPage): Page<QualityType>?{
        return qualityTypeRepository.findAll(pageOptions.toPageable())
    }
    override fun addQuality(input: com.my.hotel.server.graphql.dto.request.QualityInput): Quality? {
        logger.info("New quality: $input")
        val qualityType = qualityTypeRepository.findByIdOrNull(input.qualityTypeId!!) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"qualityTypeId")
        val quality = Quality(input.name!!, qualityType, active = input.active)
        return qualityRepository.save(quality)
    }
    override fun addQualityType(name: String): QualityType? {
        logger.info("New quality type: $name")
        val quality = QualityType(name)
        return qualityTypeRepository.save(quality)
    }
    override fun updateQuality(input: com.my.hotel.server.graphql.dto.request.UpdateQuality): Quality? {
        logger.info("Edit quality: $input")
        val quality = qualityRepository.findByIdOrNull(input.id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        if (input.active!=null){
            quality.active = input.active
        }
        if (input.name!=null){
            quality.name = input.name!!
        }
        if (input.qualityTypeId!=null){
            val qualityType = qualityTypeRepository.findByIdOrNull(input.qualityTypeId!!) ?: return null
            quality.qualityType = qualityType
        }
        return qualityRepository.save(quality)
    }
    override fun updateQualityType(input: com.my.hotel.server.graphql.dto.request.UpdateQualityType): QualityType? {
        logger.info("Edit quality type: $input")
        val qualityType = qualityTypeRepository.findByIdOrNull(input.id!!) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        if (input.name!=null){
            qualityType.name = input.name!!
        }
        return qualityTypeRepository.save(qualityType)
    }
    override fun deleteQuality(id: Long): Boolean {
        logger.info("Delete quality: $id")
        val quality = qualityRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        qualityRepository.delete(quality)
        return true
    }
    override fun deleteQualityType(id: Long): Boolean {
        logger.info("Delete quality type: $id")
        val qualityType = qualityTypeRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        val qualities = qualityRepository.findByQualityType(qualityType)
        if (!qualities.isNullOrEmpty()){
            throw AlreadyExistCustomException(Constants.QUALITY_TYPE_ALREADY_PRESENT)
        }
        qualityTypeRepository.delete(qualityType)
        return true
    }

    override fun getQualityAdmin(id: Long): Quality? {
        return qualityRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
    }

    override fun qualityTypeAdmin(id: Long): QualityType? {
        return qualityTypeRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "id")
    }
}