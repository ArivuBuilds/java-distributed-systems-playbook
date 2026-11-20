package com.arivubuilds.internals.solid.p05_isp.good;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentWorkflowRouter {

    private final List<Printable> printers;
    private final List<Scannable> scanners;

    public DocumentWorkflowRouter(List<Printable> printers, List<Scannable> scanners) {
        this.printers = printers;
        this.scanners = scanners;
    }

    public List<String> broadcastPrint(String document) {
        return printers.stream()
                .map(printer -> printer.print(document))
                .toList();
    }

    public List<String> archiveScan(String document) {
        return scanners.stream()
                .map(scanner -> scanner.scan(document))
                .toList();
    }
}
