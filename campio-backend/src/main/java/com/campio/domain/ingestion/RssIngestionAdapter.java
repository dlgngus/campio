package com.campio.domain.ingestion;

import com.campio.global.exception.BadRequestException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;

@Component
@RequiredArgsConstructor
public class RssIngestionAdapter implements IngestionAdapter {

  private final RestTemplate ingestionRestTemplate;

  @Override
  public OpportunitySourceType supports() {
    return OpportunitySourceType.RSS;
  }

  @Override
  public List<FetchedRawOpportunity> fetch(OpportunitySource source) {
    String body = ingestionRestTemplate.getForObject(source.getBaseUrl(), String.class);
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      Document document = factory.newDocumentBuilder()
          .parse(new ByteArrayInputStream((body == null ? "" : body).getBytes(StandardCharsets.UTF_8)));
      NodeList items = document.getElementsByTagName("item");
      List<FetchedRawOpportunity> results = new ArrayList<>();
      for (int index = 0; index < items.getLength(); index++) {
        Element item = (Element) items.item(index);
        String title = text(item, "title");
        String link = text(item, "link");
        if (isBlank(title) || isBlank(link)) {
          continue;
        }
        results.add(
            FetchedRawOpportunity.builder()
                .externalId(firstNonBlank(text(item, "guid"), link))
                .sourceUrl(link)
                .rawTitle(title)
                .rawContent(text(item, "description"))
                .rawPayload(elementToText(item))
                .build());
      }
      return results;
    } catch (Exception ex) {
      throw new BadRequestException("Failed to parse RSS source response");
    }
  }

  private String text(Element parent, String tagName) {
    NodeList nodes = parent.getElementsByTagName(tagName);
    if (nodes.getLength() == 0) {
      return null;
    }
    return nodes.item(0).getTextContent();
  }

  private String elementToText(Element element) {
    return element.getTextContent();
  }

  private String firstNonBlank(String preferred, String fallback) {
    return isBlank(preferred) ? fallback : preferred;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
