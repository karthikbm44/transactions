package com.onebank.transaction.Service.Impl;

import com.onebank.transaction.Dto.AddressDto;
import com.onebank.transaction.Dto.EmailDetails;
import com.onebank.transaction.Dto.UserDetailsDto;
import com.onebank.transaction.Entity.Transaction;
import com.onebank.transaction.Repository.TransactionRepository;
import com.onebank.transaction.Service.BankStatementService;
import com.onebank.transaction.Service.EmailService;
import com.onebank.transaction.Utiliy.MyTextClass;
import jakarta.mail.MessagingException;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

@Service
@NoArgsConstructor
public class BankStatementServiceImpl implements BankStatementService {

    private static final Logger log = LoggerFactory.getLogger(BankStatementServiceImpl.class);
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private WebClient webClient;


    private static final String FILE = "C:\\Users\\Karthik B M\\Documents\\bankstatements\\Statement";

    @Override
    public List<Transaction> generateBankStatement(String accountNumber, String fromDateString, String toDateString) {
        List<Transaction> transactionList = null;

        if(!StringUtils.isBlank(fromDateString) || !StringUtils.isBlank(toDateString)) {
            LocalDate  fromDate = LocalDate.parse(fromDateString, DateTimeFormatter.ISO_DATE);
            LocalDate  toDate = LocalDate.parse(toDateString, DateTimeFormatter.ISO_DATE);
            transactionList = transactionRepository.findAll().stream()
                    .filter(transaction -> transaction.getAccountNumber().equals(accountNumber))
                    .filter(transaction -> transaction.getCheckedIn().toLocalDate().isEqual(fromDate) ||
                            (transaction.getCheckedIn().toLocalDate().isAfter(fromDate) &&
                                    transaction.getCheckedIn().toLocalDate().isBefore(toDate)) ||
                            transaction.getCheckedIn().toLocalDate().isEqual(toDate))
                    .toList();
        } else {
            transactionList = transactionRepository.findAll().stream()
                    .filter(transaction -> transaction.getAccountNumber().equals(accountNumber)).toList();
        }




        try {
            designStatement(transactionList,accountNumber);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return transactionList;

    }

    private void designStatement(List<Transaction> transactions,String accountNumber) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page= new PDPage(PDRectangle.A4);
        document.addPage(page);

        String bankName="One Bank";
        String[] bankAddress ={"1973,One Bank,28thCross","Hebbal 2nd Stage,Mysuru","Karnataka-570016"};


        //getting user Details
        UserDetailsDto userDetailsDto= webClient.get()
                .uri(uriBuilder ->
                        uriBuilder.path("/userDetails")
                                .queryParam("accountNumber",accountNumber)
                                .build())
                .retrieve()
                .bodyToMono(UserDetailsDto.class)
                .block();


        assert userDetailsDto != null;
        String accountHolderName= userDetailsDto.getAccountHolderName();
        AddressDto addressDto = userDetailsDto.getAddress();
        String addressLine1 = addressDto.getHouseNoOrName()+","+addressDto.getAddressLine1()+",";
        String addressLine2 =addressDto.getAddressLine2()+","+addressDto.getCity()+","+addressDto.getState()+"-"+addressDto.getPinCode();

        String mobileAndEmail =userDetailsDto.getMobileNumber()+","+userDetailsDto.getEmailId();
        String accountNumberString = "Account Number : "+ userDetailsDto.getAccountNumber();

        int pageHeight=(int)page.getTrimBox().getHeight();
        int pageWidth=(int)page.getTrimBox().getWidth();


        PDPageContentStream contentStream = new PDPageContentStream(document,page);
        MyTextClass myTextClass = new MyTextClass(document,contentStream);

        PDFont font=PDType1Font.HELVETICA_BOLD;

        myTextClass.addSingleLineText(bankName,25,pageHeight-50,font,30,Color.black);
        myTextClass.addMultiLineText(bankAddress,10f,30,pageHeight-65,PDType1Font.HELVETICA,8,Color.darkGray);

        float accountHolderNameWidth= myTextClass.getTextWidth(accountHolderName,font,18)+25;
        float addressLine1Width=myTextClass.getTextWidth(addressLine1,PDType1Font.HELVETICA,7)+25;
        float addressLine2Width=myTextClass.getTextWidth(addressLine2,PDType1Font.HELVETICA,7)+25;
        float mobileAndEmailWidth=myTextClass.getTextWidth(mobileAndEmail,PDType1Font.HELVETICA,7)+25;
        float accountNumberStringWidth=myTextClass.getTextWidth(accountNumberString,PDType1Font.HELVETICA,7)+25;

        myTextClass.addSingleLineText(accountHolderName, (int) (pageWidth-accountHolderNameWidth),pageHeight-50,font,18,Color.black);
        myTextClass.addSingleLineText(addressLine1,(int) (pageWidth-addressLine1Width),pageHeight-60,PDType1Font.HELVETICA,7,Color.darkGray);
        myTextClass.addSingleLineText(addressLine2,(int) (pageWidth-addressLine2Width),pageHeight-70,PDType1Font.HELVETICA,7,Color.darkGray);
        myTextClass.addSingleLineText(mobileAndEmail, (int) (pageWidth-mobileAndEmailWidth),pageHeight-80,PDType1Font.HELVETICA,7,Color.darkGray);
        myTextClass.addSingleLineText(accountNumberString, (int) (pageWidth-accountNumberStringWidth),pageHeight-90,PDType1Font.HELVETICA,7,Color.darkGray);

        contentStream.setStrokingColor(Color.DARK_GRAY);
        contentStream.addRect(25,pageHeight-100,540,1);
        contentStream.fill();



        String headLine="Bank Statement";
        float headLineWidth=myTextClass.getTextWidth(headLine,font,25);
        myTextClass.addSingleLineText(headLine,(int) (pageWidth-headLineWidth)/2,pageHeight-125,PDType1Font.TIMES_BOLD,25,Color.black);

        contentStream.setStrokingColor(Color.DARK_GRAY);
        contentStream.addRect(25,pageHeight-135,540,1);
        contentStream.fill();


        // Draw table
        contentStream.setStrokingColor(Color.GRAY);
        contentStream.setLineWidth(1);
        int intX = 25;
        int intY = pageHeight - 150;
        int cellHeight = 30;
        int cellWidth = 135;
        //int dateCellWidth=159;

        // Draw header
        String[] header = {"Date", "Withdraw", "Deposit","Available"};
        int colCount = header.length;

        // Draw header cells
        for (int j = 0; j < colCount; j++) {
            // Set background color
            contentStream.setNonStrokingColor(Color.BLACK);
            //int currentCellWidth=(j==0)? dateCellWidth:cellWidth;
            contentStream.addRect(intX, intY, cellWidth, -cellHeight);
            contentStream.fill();
            //set font colour
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(intX+50, intY - 20);
            contentStream.showText(header[j]);
            contentStream.endText();

            intX += cellWidth;
        }
        intX = 25;
        intY -= cellHeight;
        // Draw transaction rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm");

        for (Transaction transaction : transactions) {
            String[] rowData = {
                    transaction.getCheckedIn().format(formatter),
                    String.valueOf(transaction.getDebitAmount()),
                    String.valueOf(transaction.getCreditAmount()),
                    transaction.getCurrentBalance()
            };

            for (int j = 0; j < colCount; j++) {
                contentStream.setNonStrokingColor(Color.lightGray);
               // int currentCellWidth=(j==0)? dateCellWidth:cellWidth;
                contentStream.addRect(intX, intY, cellWidth, -cellHeight);
                contentStream.fill();

                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(intX + 50, intY - 20);
                contentStream.showText(rowData[j]);
                contentStream.endText();

                intX += cellWidth;
            }

            intX = 25;
            intY -= cellHeight;

            //creatind new page if needed
            if (intY < 50) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream =new PDPageContentStream(document, page);
                intY = pageHeight - 25;
            }
        }
        contentStream.stroke();
        contentStream.close();

        //setting the file author details
        PDDocumentInformation information = new PDDocumentInformation();

        information.setAuthor("One Bank");
        information.setCreator("Auto Generated");
        information.setTitle("Bank Statement");
        information.setSubject("Statement of "+userDetailsDto.getAccountHolderName());
        information.setCreationDate(Calendar.getInstance());

        document.setDocumentInformation(information);

        //encryting the file
        final int keyLength = 128;
        AccessPermission accessPermission= new AccessPermission();
        accessPermission.setCanPrint(false);

        String useName = userDetailsDto.getAccountHolderName();
        String userPassword =useName.substring(0,4).toUpperCase()+accountNumber.substring(6);

        StandardProtectionPolicy sp = new StandardProtectionPolicy("0406",userPassword,accessPermission);
        sp.setEncryptionKeyLength(keyLength);
        sp.setPermissions(accessPermission);

        document.protect(sp);

        String fileName = FILE+"_"+userDetailsDto.getAccountNumber().substring(0,3)+"XXXX"+userDetailsDto.getAccountNumber().substring(7)+".pdf";
        document.save(fileName);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(userDetailsDto.getEmailId())//this mail id is hardcoded u need to create an endpoint to get all the details of the customer
                .subject("Bank Statement")
                .messageBody("Dear Customer,\n\nTo open the statement use the first 4 letter of your name in capital with the last 4 digits of you account number." +
                        "\nFor example:-\nAccount Holder Name: Abcdefgh Ijk Lmn \nAccount Number: 1234567890 \nYour password will be ABCD7890." +
                        "\n\nPlease find your requested bank statement attached!\n\nSincerely,\nOne Bank Team")
                .attachment(fileName)
                .build();

        try {
            emailService.sendEmailWithAttachment(emailDetails);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("PDF created successfully.");
    }
}
