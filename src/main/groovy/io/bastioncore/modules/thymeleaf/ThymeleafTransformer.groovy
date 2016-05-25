package io.bastioncore.modules.thymeleaf

import io.bastioncore.core.Configuration
import io.bastioncore.core.ContextHolder;
import io.bastioncore.core.components.AbstractTransformer;
import io.bastioncore.core.messages.DefaultMessage
import io.bastioncore.core.resolvers.IResourceResolver
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.AbstractTemplateResolver
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Component
@Scope('prototype')
public class ThymeleafTransformer extends AbstractTransformer{

    private AbstractTemplateResolver templateResolver

    private TemplateEngine templateEngine

    private String layout


    void onReceive(def message){
        super.onReceive(message)
        if (message instanceof Configuration){
            layout = message.configuration.layout
            if(!layout && message.configuration.layout_id){
                String resolverId = message.configuration.resolver ?: 'fileSystemResolver'
                IResourceResolver resolver = ContextHolder.applicationContext.getBean(resolverId)
                layout = resolver.getResource(type:'layout',name:message.configuration.layout_id)
            }
            templateResolver = new StringTemplateResolver()
            templateEngine = new TemplateEngine()
            templateEngine.setTemplateResolver(templateResolver)
        }
    }

    @Override
    DefaultMessage process(DefaultMessage message) {
        Context context = new Context()
        context.setVariable('content',message.content)
        context.setVariable('context',message.context)
        String data = templateEngine.process(layout,context)
        return new DefaultMessage(data, message.context)
    }
}
