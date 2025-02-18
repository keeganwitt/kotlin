/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.symbols.KtFirSymbol
import org.jetbrains.kotlin.analysis.api.fir.utils.withSymbolAttachment
import org.jetbrains.kotlin.analysis.api.impl.base.CachingKtAnalysisSessionProvider
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbol
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.LLFirResolveSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getFirResolveSession
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.analysis.utils.errors.buildErrorWithAttachment

@OptIn(KtAnalysisApiInternals::class)
class KtFirAnalysisSessionProvider(project: Project) : CachingKtAnalysisSessionProvider<LLFirResolveSession>(project) {
    override fun getFirResolveSession(contextElement: KtElement): LLFirResolveSession {
        return contextElement.getFirResolveSession()
    }

    override fun getFirResolveSession(contextSymbol: KtSymbol): LLFirResolveSession {
        return when (contextSymbol) {
            is KtFirSymbol<*> -> contextSymbol.firResolveSession
            else -> buildErrorWithAttachment("Invalid symbol") {
                withSymbolAttachment("symbol", contextSymbol)
            }
        }
    }

    override fun getFirResolveSession(contextModule: KtModule): LLFirResolveSession {
        checkNotNull(contextModule.project)
        return contextModule.getFirResolveSession(contextModule.project!!)
    }

    override fun createAnalysisSession(
        firResolveSession: LLFirResolveSession,
        token: KtLifetimeToken,
    ): KtAnalysisSession {
        @Suppress("DEPRECATION")
        return KtFirAnalysisSession.createAnalysisSessionByFirResolveSession(firResolveSession, token)
    }
}


