package com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz

import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.ArtistCreditDto
import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzArtistDto
import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzRecordingDto
import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzReleaseDto
import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzReleaseGroupDto
import com.bandage.musicmetadataservice.domain.model.Artist
import com.bandage.musicmetadataservice.domain.model.ArtistRef
import com.bandage.musicmetadataservice.domain.model.Recording
import com.bandage.musicmetadataservice.domain.model.Release
import com.bandage.musicmetadataservice.domain.model.ReleaseGroup

internal fun MusicBrainzRecordingDto.toDomain(): Recording =
    Recording(
        id = id,
        title = title,
        lengthMs = length,
        artists = artistCredit.toArtistRefs(),
        isrcs = isrcs ?: emptyList(),
        releaseCount = releases?.size ?: 0,
        score = score,
    )

internal fun MusicBrainzArtistDto.toDomain(): Artist =
    Artist(
        id = id,
        name = name,
        sortName = sortName,
        country = country,
        type = type,
        score = score,
    )

internal fun MusicBrainzReleaseGroupDto.toDomain(): ReleaseGroup =
    ReleaseGroup(
        id = id,
        title = title,
        primaryType = primaryType,
        secondaryTypes = secondaryTypes ?: emptyList(),
        firstReleaseDate = firstReleaseDate,
        artistCredit = artistCredit.toArtistRefs(),
        releaseCount = releases?.size ?: 0,
        score = score,
    )

internal fun MusicBrainzReleaseDto.toDomain(): Release =
    Release(
        id = id,
        title = title,
        date = date,
        country = country,
        artistCredit = artistCredit.toArtistRefs(),
    )

private fun List<ArtistCreditDto>?.toArtistRefs(): List<ArtistRef> =
    this?.map { ArtistRef(id = it.artist.id, name = it.artist.name) } ?: emptyList()
